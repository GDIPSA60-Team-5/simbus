import unittest
from datetime import datetime, timedelta
from unittest.mock import patch

# Import the functions you want to test
from llm.utils import (
    typewriter_print,
    merge_slots,
    extract_json_from_response,
    show_help,
    get_recent_history,
    get_user_context,
    reset_conversation_for_user,
    convert_slot_value,
    validate_future_datetime,
    current_datetime,
    serialize_for_json,
    find_missing_slots,
    flatten_slots,
)
from llm.state import SLOT_TYPES, REQUIRED_SLOTS, user_conversations


class TestUtils(unittest.TestCase):
    def test_typewriter_print(self):
        # Redirect stdout to capture printed output
        with patch("sys.stdout.write") as mock_write, patch("time.sleep") as mock_sleep:
            typewriter_print("abc", delay=0)
        mock_write.assert_any_call("a")
        mock_sleep.assert_called()

    def test_merge_slots_valid_datetime(self):
        now = datetime.now() + timedelta(hours=1)
        SLOT_TYPES["arrival_time"] = datetime
        current = {"arrival_time": None}
        new = {"arrival_time": now.isoformat()}
        merge_slots(current, new)
        self.assertIsInstance(current["arrival_time"], datetime)

    def test_merge_slots_invalid_future(self):
        past = datetime.now() - timedelta(hours=1)
        SLOT_TYPES["arrival_time"] = datetime
        current = {"arrival_time": None}
        new = {"arrival_time": past.isoformat()}
        merge_slots(current, new)
        self.assertIsNone(current["arrival_time"])

    def test_extract_json_from_response_valid(self):
        text = 'Here is JSON: {"key": "value"}'
        result = extract_json_from_response(text)
        self.assertEqual(result, {"key": "value"})

    def test_extract_json_from_response_invalid(self):
        text = "No JSON here"
        result = extract_json_from_response(text)
        self.assertIsNone(result)

    def test_show_help_contains_username(self):
        help_text = show_help("Alex")
        self.assertIn("Hi Alex", help_text)

    def test_get_recent_history(self):
        history = list(range(10))
        recent = get_recent_history(history, 3)
        self.assertEqual(recent, [7, 8, 9])

    def test_get_user_context_creates_new(self):
        user_conversations.clear()
        context = get_user_context("Bob")
        self.assertIn("Bob", user_conversations)
        self.assertEqual(context["state"]["intent"], None)

    def test_reset_conversation_for_user(self):
        get_user_context("Eve")
        user_conversations["Eve"]["state"]["intent"] = "test"
        reset_conversation_for_user("Eve")
        self.assertIsNone(user_conversations["Eve"]["state"]["intent"])

    def test_convert_slot_value_datetime_iso(self):
        SLOT_TYPES["test_time"] = datetime
        iso_str = (datetime.now()).isoformat()
        result = convert_slot_value("test_time", iso_str)
        self.assertIsInstance(result, datetime)

    def test_convert_slot_value_time_format(self):
        SLOT_TYPES["test_time"] = datetime
        result = convert_slot_value("test_time", "10:30")
        self.assertIsInstance(result, datetime)

    def test_convert_slot_value_str(self):
        SLOT_TYPES["test_str"] = str
        self.assertEqual(convert_slot_value("test_str", 123), "123")

    def test_validate_future_datetime_future(self):
        future = datetime.now() + timedelta(days=1)
        self.assertTrue(validate_future_datetime(future))

    def test_validate_future_datetime_past(self):
        past = datetime.now() - timedelta(days=1)
        self.assertFalse(validate_future_datetime(past))

    def test_current_datetime_timezone(self):
        dt = current_datetime()
        self.assertEqual(getattr(dt.tzinfo, "zone", None), "Asia/Singapore")

    def test_serialize_for_json(self):
        dt = datetime(2024, 1, 1)
        obj = {"a": dt, "b": [dt]}
        result: dict = serialize_for_json(obj)
        self.assertEqual(result["a"], dt.isoformat())

    def test_find_missing_slots_simple(self):
        REQUIRED_SLOTS["test_intent"] = ["slot1", "slot2"]
        current_slots = {"slot1": "ok", "slot2": None}
        missing = find_missing_slots("test_intent", current_slots)
        self.assertEqual(missing, ["slot2"])

    def test_find_missing_slots_group(self):
        REQUIRED_SLOTS["test_intent_group"] = [["alt1", "alt2"]]
        current_slots = {"alt1": None, "alt2": None}
        missing = find_missing_slots("test_intent_group", current_slots)
        self.assertEqual(missing, ["alt1", "alt2"])

    def test_flatten_slots(self):
        required = [["a", "b"], "c"]
        self.assertEqual(flatten_slots(required), ["a", "b", "c"])


if __name__ == "__main__":
    unittest.main()
