# Submit Listening — API Documentation

## Endpoint

# Listening
```
POST /api/v1/mock/listening
```

# Reading
```
POST /api/v1/mock/reading
```
---

## Request Body

```json
{
  "mockQuizId": "uuid",
  "sections": [
    {
      "sectionRecordId": "uuid",
      "answers": [
        {
          "problemId": "uuid",
          "answers": {
            "orderIndex": "submitted text"
          }
        }
      ]
    }
  ]
}
```

---

## Field Reference

| Field | Type | Required | Description |
|---|---|---|---|
| `mockQuizId` | UUID | ✅ | The mock quiz session ID |
| `sections` | Array | ✅ | List of section submissions |
| `sections[].sectionRecordId` | UUID | ✅ | The section record ID for this section |
| `sections[].answers` | Array | ✅ | List of problem answers. Send empty array `[]` if user skipped all problems in this section |
| `answers[].problemId` | UUID | ✅ | The problem being answered |
| `answers[].answers` | Map | ✅ | Key-value map of `orderIndex → submitted text`. See problem types below |

> **Note:** Every problem in a section must always be included regardless of whether the user answered it or not. If a problem was skipped, include it with an empty `answers` map `{}`.

---

## Problem Types & Answer Format

### INPUT
Free-text fill-in-the-blank. One answer per problem.

```json
{
  "problemId": "9fe9c4b4-16d2-4946-9c4e-4dd3dc18dfd9",
  "answers": {
    "1": "snails"
  }
}
```

The key (`"1"`) is the problem's `orderIndex`. The value is what the user typed.

---

### RADIO / SELECT
Multiple choice — user picks one option. Send the `orderIndex` of the option the user selected as the key, and its display text as the value.

```json
{
  "problemId": "14c4117c-b380-4714-8aeb-dc43ac8c25e5",
  "answers": {
    "8-2": "FALSE"
  }
}
```

The key (`"8-2"`) is the selected option's `orderIndex`. The value is its display text (e.g. `"TRUE"`, `"FALSE"`, `"NOT GIVEN"`).

---

### MULTI_SELECT
Two grouped problems — user picks two options (one per slot). Send both, one entry per slot.

```json
{
  "problemId": "e7f1a2b3-0000-0000-0000-000000000001",
  "answers": {
    "1_2-1": "You are good",
    "1_2-2": "You are ugly"
  }
}
```

The keys (`"1_2-1"`, `"1_2-2"`) are the selected options' `orderIndex` values. The values are their display texts.

> **Important:** The backend grades MULTI_SELECT by value, not by which slot key you used. It does not matter if the user picks `"You are good"` under key `"1_2-1"` or `"1_2-2"` — both will be graded correctly. However, always send exactly the key of the option the user actually selected.

---

## Empty / Skipped Answers

If a user skips a problem entirely, still include it with an empty map:

```json
{
  "problemId": "9d5f3b88-8851-4cd3-8e64-2ea1238ef4e3",
  "answers": {}
}
```

If the user skips all problems in a section, still include the section with an empty answers array:

```json
{
  "sectionRecordId": "b44140c5-e9aa-4fc3-9253-591e5385e7c8",
  "answers": []
}
```

---

## Full Example

```json
{
  "mockQuizId": "a1b2c3d4-0000-0000-0000-000000000001",
  "sections": [
    {
      "sectionRecordId": "b44140c5-e9aa-4fc3-9253-591e5385e7c8",
      "answers": [
        {
          "problemId": "9fe9c4b4-16d2-4946-9c4e-4dd3dc18dfd9",
          "answers": { "1": "snails" }
        },
        {
          "problemId": "9d5f3b88-8851-4cd3-8e64-2ea1238ef4e3",
          "answers": { "2": "expensive" }
        },
        {
          "problemId": "e25073e6-bfb2-47c7-bc4f-9e01ebffc05a",
          "answers": {}
        },
        {
          "problemId": "14c4117c-b380-4714-8aeb-dc43ac8c25e5",
          "answers": { "8-2": "FALSE" }
        },
        {
          "problemId": "e7f1a2b3-0000-0000-0000-000000000001",
          "answers": {
            "1_2-1": "You are good",
            "1_2-2": "You are ugly"
          }
        }
      ]
    }
  ]
}
```

---

## Order Index Format Reference

| Problem type | Example `orderIndex` | Meaning |
|---|---|---|
| INPUT | `"1"`, `"2"`, `"3"` | Problem number |
| RADIO / SELECT | `"8-1"`, `"8-2"`, `"8-3"` | Problem `8`, option `1/2/3` |
| MULTI_SELECT | `"1_2-1"`, `"1_2-2"` | Problems `1` and `2` grouped, option `1/2` |