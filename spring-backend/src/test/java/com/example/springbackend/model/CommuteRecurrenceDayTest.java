package com.example.springbackend.model;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

public class CommuteRecurrenceDayTest {

    private static Validator validator;

    @BeforeAll
    public static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testBuilderAndGetters() {
        CommuteRecurrenceDay day = CommuteRecurrenceDay.builder()
                .id("rec123")
                .dayOfWeek(3)
                .commutePlanId("plan456")
                .build();

        assertEquals("rec123", day.getId());
        assertEquals(3, day.getDayOfWeek());
        assertEquals("plan456", day.getCommutePlanId());
    }

    @Test
    public void testNoArgsConstructorAndSetters() {
        CommuteRecurrenceDay day = new CommuteRecurrenceDay();

        day.setId("rec789");
        day.setDayOfWeek(5);
        day.setCommutePlanId("plan987");

        assertEquals("rec789", day.getId());
        assertEquals(5, day.getDayOfWeek());
        assertEquals("plan987", day.getCommutePlanId());
    }

    @Test
    public void testValidation_MinMaxConstraints() {
        // dayOfWeek less than 1 (invalid)
        CommuteRecurrenceDay invalidLow = CommuteRecurrenceDay.builder()
                .dayOfWeek(0)
                .build();

        Set<ConstraintViolation<CommuteRecurrenceDay>> violationsLow = validator.validate(invalidLow);
        assertFalse(violationsLow.isEmpty());
        assertTrue(violationsLow.stream().anyMatch(v -> v.getMessage().contains("must be greater than or equal to 1")));

        // dayOfWeek greater than 7 (invalid)
        CommuteRecurrenceDay invalidHigh = CommuteRecurrenceDay.builder()
                .dayOfWeek(8)
                .build();

        Set<ConstraintViolation<CommuteRecurrenceDay>> violationsHigh = validator.validate(invalidHigh);
        assertFalse(violationsHigh.isEmpty());
        assertTrue(violationsHigh.stream().anyMatch(v -> v.getMessage().contains("must be less than or equal to 7")));

        // dayOfWeek valid value
        CommuteRecurrenceDay validDay = CommuteRecurrenceDay.builder()
                .dayOfWeek(4)
                .build();

        Set<ConstraintViolation<CommuteRecurrenceDay>> violationsValid = validator.validate(validDay);
        assertTrue(violationsValid.isEmpty());
    }

    @Test
    public void testEqualsHashCodeAndToString() {
        CommuteRecurrenceDay day1 = CommuteRecurrenceDay.builder()
                .id("rec123")
                .dayOfWeek(3)
                .commutePlanId("plan456")
                .build();

        CommuteRecurrenceDay day2 = CommuteRecurrenceDay.builder()
                .id("rec123")
                .dayOfWeek(3)
                .commutePlanId("plan456")
                .build();

        CommuteRecurrenceDay day3 = CommuteRecurrenceDay.builder()
                .id("rec789")
                .dayOfWeek(5)
                .commutePlanId("plan987")
                .build();

        // equals and hashCode positive case
        assertEquals(day1, day2);
        assertEquals(day1.hashCode(), day2.hashCode());

        // equals and hashCode negative case
        assertNotEquals(day1, day3);
        assertNotEquals(day1.hashCode(), day3.hashCode());

        // equals null and different class
        assertNotEquals(null, day1);
        assertNotEquals("some string", day1);

        // toString contains key properties
        String toString = day1.toString();
        assertTrue(toString.contains("rec123"));
        assertTrue(toString.contains("3"));
        assertTrue(toString.contains("plan456"));
    }
}
