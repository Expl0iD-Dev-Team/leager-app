package com.ledger.app.data

import com.ledger.app.data.repository.AccountRepository
import com.ledger.app.data.repository.CategoryRepository
import com.ledger.app.data.repository.TransactionRepository
import com.ledger.app.domain.model.Account
import com.ledger.app.domain.model.AccountType
import com.ledger.app.domain.model.Category
import com.ledger.app.domain.model.CategoryType
import com.ledger.app.domain.model.Transaction
import com.ledger.app.domain.model.TransactionType
import kotlinx.coroutines.flow.first
import java.io.InputStream
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.math.abs

class CsvImporter(
    private val accountRepo: AccountRepository,
    private val categoryRepo: CategoryRepository,
    private val transactionRepo: TransactionRepository
) {
    data class Result(val imported: Int, val skipped: Int, val errors: List<String>)

    // Column name aliases (lowercase)
    private val dateAliases    = setOf("date", "дата")
    private val amountAliases  = setOf("amount", "сумма", "sum")
    private val typeAliases    = setOf("type", "тип")
    private val accountAliases = setOf("account", "счёт", "счет", "from_account", "from account", "from")
    private val toAccAliases   = setOf("to_account", "to account", "to", "счёт назначения", "счет назначения", "кому", "куда")
    private val catAliases     = setOf("category", "категория", "cat")
    private val noteAliases    = setOf("note", "comment", "комментарий", "заметка", "описание", "notes")
    private val uidAliases     = setOf("uid", "id", "uuid")

    // Transaction type aliases (lowercase)
    private val incomeValues   = setOf("income", "доход", "приход", "in", "+")
    private val expenseValues  = setOf("expense", "расход", "трата", "out", "-")
    private val transferValues = setOf("transfer", "перевод", "между счетами")

    private val dateFormatters = listOf(
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("dd.MM.yyyy"),
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
        DateTimeFormatter.ofPattern("MM/dd/yyyy"),
        DateTimeFormatter.ofPattern("d.M.yyyy"),
        DateTimeFormatter.ofPattern("d/M/yyyy"),
        DateTimeFormatter.ofPattern("yyyy.MM.dd"),
    )

    suspend fun import(stream: InputStream): Result {
        val accountCache = accountRepo.getAllAccounts().first()
            .associateBy { it.name.trim().lowercase() }
            .toMutableMap<String, Account>()

        val categoryCache = categoryRepo.getAll().first()
            .associateBy { "${it.type.name}:${it.name.trim().lowercase()}" }
            .toMutableMap<String, Category>()

        // Strip UTF-8 BOM (EF BB BF) that Excel adds when saving as "UTF-8 with BOM"
        val bytes = stream.readBytes()
        val hasBom = bytes.size >= 3
            && bytes[0] == 0xEF.toByte()
            && bytes[1] == 0xBB.toByte()
            && bytes[2] == 0xBF.toByte()
        val text = String(bytes, if (hasBom) 3 else 0, bytes.size - if (hasBom) 3 else 0, Charsets.UTF_8)
        val lines = text.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) return Result(0, 0, emptyList())

        val sep = detectSeparator(lines[0])
        val headerCols = parseLine(lines[0], sep).map { it.trim().lowercase() }
        val colMap = buildColumnMap(headerCols)

        // Check required columns
        val missing = listOf(
            "date"    to "date / дата",
            "amount"  to "amount / сумма",
            "type"    to "type / тип",
            "account" to "account / счёт"
        ).filter { (k, _) -> colMap[k] == null }.map { (_, label) -> label }

        if (missing.isNotEmpty()) {
            return Result(0, 0, listOf(
                "Required columns not found: ${missing.joinToString(", ")}.\n" +
                "Check the file header (first line)."
            ))
        }

        var imported = 0
        var skipped  = 0
        val errors   = mutableListOf<String>()

        lines.drop(1).forEachIndexed { idx, raw ->
            val rowNum = idx + 2
            try {
                val cols = parseLine(raw, sep)
                fun cell(key: String) = colMap[key]?.let { cols.getOrElse(it) { "" }.trim() }.orEmpty()

                val accName   = cell("account")
                val dateStr   = cell("date")
                val typeStr   = cell("type")
                // Normalize amount: remove spaces, replace decimal comma with dot
                val amountStr = cell("amount").filter { !it.isWhitespace() }.replace(",", ".")
                val toAccName = cell("to_account")
                val catName   = cell("category")
                val note      = cell("note")
                val uidStr    = cell("uid")

                if (accName.isBlank()) {
                    skipped++
                    errors.add("Row $rowNum: the “account” field is empty")
                    return@forEachIndexed
                }

                val date = parseDate(dateStr)
                    ?: throw IllegalArgumentException(
                        "Invalid date “$dateStr” — use YYYY-MM-DD or DD.MM.YYYY"
                    )

                val rawAmount = amountStr.toDoubleOrNull()?.let { abs(it) }
                    ?: throw IllegalArgumentException("Invalid amount “$amountStr”")

                val txType = parseType(typeStr)
                    ?: throw IllegalArgumentException(
                        "Unknown type “$typeStr” — expected income, expense or transfer"
                    )

                val amount  = if (txType == TransactionType.INCOME) rawAmount else -rawAmount
                val account = resolveAccount(accName, accountCache)
                val toAccount = if (txType == TransactionType.TRANSFER && toAccName.isNotBlank())
                    resolveAccount(toAccName, accountCache) else null
                val category = resolveCategory(catName, txType, categoryCache)

                val txId  = uidStr.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString()
                val isNew = transactionRepo.getById(txId) == null

                transactionRepo.save(
                    Transaction(
                        id          = txId,
                        amount      = amount,
                        type        = txType,
                        categoryId  = category.id,
                        accountId   = account.id,
                        toAccountId = toAccount?.id,
                        note        = note,
                        date        = date,
                        time        = LocalTime.of(0, 0)
                    )
                )
                imported++

                if (isNew) {
                    accountRepo.adjustBalance(account.id, amount)
                    if (txType == TransactionType.TRANSFER && toAccount != null)
                        accountRepo.adjustBalance(toAccount.id, rawAmount)
                }
            } catch (e: Exception) {
                errors.add("Row $rowNum: ${e.message}")
                skipped++
            }
        }

        return Result(imported, skipped, errors)
    }

    // ── Separator detection ──────────────────────────────────────────────────

    private fun detectSeparator(header: String): Char {
        val counts = mapOf(
            ','  to header.count { it == ',' },
            ';'  to header.count { it == ';' },
            '\t' to header.count { it == '\t' }
        )
        return counts.maxByOrNull { it.value }?.takeIf { it.value > 0 }?.key ?: ','
    }

    // ── Column mapping ───────────────────────────────────────────────────────

    private fun buildColumnMap(headers: List<String>): Map<String, Int> {
        val map = mutableMapOf<String, Int>()
        headers.forEachIndexed { i, col ->
            when (col) {
                in dateAliases    -> map["date"]       = i
                in amountAliases  -> map["amount"]     = i
                in typeAliases    -> map["type"]       = i
                in accountAliases -> map["account"]    = i
                in toAccAliases   -> map["to_account"] = i
                in catAliases     -> map["category"]   = i
                in noteAliases    -> map["note"]       = i
                in uidAliases     -> map["uid"]        = i
            }
        }
        return map
    }

    // ── Parsers ──────────────────────────────────────────────────────────────

    private fun parseDate(s: String): LocalDate? {
        for (fmt in dateFormatters) {
            try { return LocalDate.parse(s, fmt) } catch (_: Exception) {}
        }
        return null
    }

    private fun parseType(s: String): TransactionType? = when (s.trim().lowercase()) {
        in incomeValues   -> TransactionType.INCOME
        in expenseValues  -> TransactionType.EXPENSE
        in transferValues -> TransactionType.TRANSFER
        else              -> null
    }

    private fun parseLine(line: String, sep: Char): List<String> {
        val result = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false
        for (ch in line) {
            when {
                ch == '"'              -> inQuotes = !inQuotes
                ch == sep && !inQuotes -> { result.add(sb.toString()); sb.clear() }
                else                   -> sb.append(ch)
            }
        }
        result.add(sb.toString())
        return result
    }

    // ── Resolvers ────────────────────────────────────────────────────────────

    private suspend fun resolveAccount(
        name: String,
        cache: MutableMap<String, Account>
    ): Account {
        val key = name.lowercase()
        return cache[key] ?: run {
            val new = Account(
                id        = UUID.randomUUID().toString(),
                name      = name,
                currency  = "RUB",
                balance   = 0.0,
                type      = AccountType.CARD,
                color     = COLORS[cache.size % COLORS.size],
                sortOrder = cache.size
            )
            accountRepo.save(new)
            cache[key] = new
            new
        }
    }

    private suspend fun resolveCategory(
        name: String,
        txType: TransactionType,
        cache: MutableMap<String, Category>
    ): Category {
        val catType = if (txType == TransactionType.INCOME) CategoryType.INCOME else CategoryType.EXPENSE
        // Blank category -> a catch-all one; reuse a legacy Russian-named one if it already exists
        val effectiveName = name.ifBlank {
            val candidates = if (catType == CategoryType.INCOME) listOf("Other income", "Прочие доходы")
                else listOf("Other expenses", "Прочие расходы")
            candidates.firstOrNull { cache.containsKey("${catType.name}:${it.lowercase()}") } ?: candidates.first()
        }
        val key = "${catType.name}:${effectiveName.lowercase()}"
        return cache[key] ?: run {
            val new = Category(
                id        = UUID.randomUUID().toString(),
                name      = effectiveName,
                iconCode  = "other",
                color     = COLORS[cache.size % COLORS.size],
                type      = catType,
                sortOrder = cache.size
            )
            categoryRepo.save(new)
            cache[key] = new
            new
        }
    }

    // ── Template ─────────────────────────────────────────────────────────────

    companion object {
        /**
         * Minimal template the user can open in Excel / Google Sheets,
         * fill in rows and import back into the app.
         */
        const val TEMPLATE_CSV = "date,amount,type,account,category,to_account,note\n" +
            "2024-01-15,500,expense,Sber,Groceries,,Supermarket\n" +
            "2024-01-16,50000,income,Tinkoff,Salary,,January\n" +
            "2024-01-17,10000,transfer,Sber,,Tinkoff,Top up card"

        /** Human-readable format description shown in the UI. */
        const val FORMAT_DESCRIPTION =
            "Required columns (any order; English or Russian headers):\n" +
            "  date      — YYYY-MM-DD or DD.MM.YYYY\n" +
            "  amount    — positive number\n" +
            "  type      — income · expense · transfer\n" +
            "  account   — account the money leaves (or arrives to, for income)\n\n" +
            "Optional columns:\n" +
            "  category\n" +
            "  to_account — destination account, for transfers\n" +
            "  note\n\n" +
            "Separator: comma, semicolon or Tab.\n" +
            "Encoding: UTF-8 (or UTF-8 with BOM from Excel).\n" +
            "Decimals: dot or comma (500.50 or 500,50)."

        private val COLORS = listOf(
            "#C5FF4A", "#FF6B5B", "#52E0C4", "#9B8BFF",
            "#FFD24A", "#FF9DC4", "#7BB8FF", "#A0A0A0"
        )
    }
}
