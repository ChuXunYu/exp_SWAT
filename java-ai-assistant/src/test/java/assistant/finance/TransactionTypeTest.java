package assistant.finance;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TransactionTypeTest {
    @Test
    void exposesFixedTypeValuesInDeclaredOrder() {
        assertArrayEquals(
                new TransactionType[] {TransactionType.INCOME, TransactionType.EXPENSE},
                TransactionType.values());
    }

    @Test
    void incomeAndExpensePredicatesAreMutuallyExclusive() {
        assertTrue(TransactionType.INCOME.isIncome());
        assertFalse(TransactionType.INCOME.isExpense());
        assertTrue(TransactionType.EXPENSE.isExpense());
        assertFalse(TransactionType.EXPENSE.isIncome());
    }

    @Test
    void valueOfParsesDeclaredTypeName() {
        assertEquals(TransactionType.INCOME, TransactionType.valueOf("INCOME"));
        assertEquals(TransactionType.EXPENSE, TransactionType.valueOf("EXPENSE"));
    }

    @Test
    void valueOfRejectsUnknownTypeName() {
        assertThrows(IllegalArgumentException.class, () -> TransactionType.valueOf("TRANSFER"));
    }

    @Test
    void nameUsesStableEnumConstantName() {
        assertEquals("INCOME", TransactionType.INCOME.name());
        assertEquals("EXPENSE", TransactionType.EXPENSE.name());
    }
}
