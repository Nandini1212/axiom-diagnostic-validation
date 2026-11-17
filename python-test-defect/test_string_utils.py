from string_utils import is_palindrome


def test_is_palindrome_detects_racecar():
    assert is_palindrome("racecar") is True


def test_is_palindrome_rejects_hello():
    assert is_palindrome("hello") is False


def test_is_palindrome_detects_empty_string():
    assert is_palindrome("") is True
