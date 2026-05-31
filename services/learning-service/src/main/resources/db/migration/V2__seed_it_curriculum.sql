-- Seed: original IT curriculum for IT-Shaharcha.
--
-- All lesson text below is written originally for this project. Where we point
-- learners to deeper material we LINK to open-licensed references (MDN under
-- CC-BY-SA, Wikipedia under CC-BY-SA, and official language/tool docs) with
-- attribution. No third-party copyrighted prose is copied here.
--
-- IDs are fixed so the graph (tracks -> courses -> modules -> lessons) is
-- stable and re-runnable. Bodies use dollar-quoting ($md$ ... $md$) so Markdown
-- apostrophes and quotes need no escaping.

-- ============================================================
-- Tracks
-- ============================================================
INSERT INTO tracks (id, title, slug, description, created_by) VALUES
  ('a1000000-0000-0000-0000-000000000001', 'Programming Fundamentals', 'programming-fundamentals',
   'Start here. Learn how to think like a programmer and write your first real code in Python and Java.', 'seed'),
  ('a1000000-0000-0000-0000-000000000002', 'Data Structures & Algorithms', 'dsa',
   'The toolkit every strong engineer shares: how to store data efficiently and reason about the cost of your code.', 'seed'),
  ('a1000000-0000-0000-0000-000000000003', 'Web Development', 'web-development',
   'Build for the browser. HTML, CSS, and JavaScript from first principles up to modern, interactive pages.', 'seed'),
  ('a1000000-0000-0000-0000-000000000004', 'Databases & SQL', 'databases-sql',
   'Store, query, and combine data with confidence using relational databases and SQL.', 'seed');

-- ============================================================
-- Courses
-- ============================================================
INSERT INTO courses (id, track_id, title, slug, summary, level, estimated_minutes, created_by) VALUES
  ('b2000000-0000-0000-0000-000000000001', 'a1000000-0000-0000-0000-000000000001',
   'Python for Beginners', 'python-for-beginners',
   'Write your first programs in Python: variables, control flow, functions, and the core data types you will use every day.',
   'beginner', 240, 'seed'),
  ('b2000000-0000-0000-0000-000000000002', 'a1000000-0000-0000-0000-000000000001',
   'Java Fundamentals', 'java-fundamentals',
   'Learn the Java platform and object-oriented programming, from variables and methods to classes and interfaces.',
   'beginner', 300, 'seed'),
  ('b2000000-0000-0000-0000-000000000003', 'a1000000-0000-0000-0000-000000000002',
   'Core Data Structures', 'core-data-structures',
   'Arrays, linked lists, stacks, queues, hash tables, trees, and graphs — what they are and when to reach for each.',
   'intermediate', 360, 'seed'),
  ('b2000000-0000-0000-0000-000000000004', 'a1000000-0000-0000-0000-000000000002',
   'Algorithmic Thinking', 'algorithmic-thinking',
   'Big-O analysis, searching and sorting, recursion, and the problem-solving patterns that show up in interviews and real work.',
   'intermediate', 300, 'seed'),
  ('b2000000-0000-0000-0000-000000000005', 'a1000000-0000-0000-0000-000000000003',
   'HTML & CSS Foundations', 'html-css-foundations',
   'Structure pages with HTML and style them with CSS, including the box model, Flexbox, and Grid.',
   'beginner', 240, 'seed'),
  ('b2000000-0000-0000-0000-000000000006', 'a1000000-0000-0000-0000-000000000003',
   'JavaScript Essentials', 'javascript-essentials',
   'The language of the web: values and functions, manipulating the DOM, and modern asynchronous JavaScript.',
   'beginner', 300, 'seed'),
  ('b2000000-0000-0000-0000-000000000007', 'a1000000-0000-0000-0000-000000000004',
   'SQL Fundamentals', 'sql-fundamentals',
   'Query relational data with SELECT, filter and sort results, aggregate with GROUP BY, and combine tables with joins.',
   'beginner', 240, 'seed');

-- ============================================================
-- Modules
-- ============================================================
INSERT INTO modules (id, course_id, title, order_index, created_by) VALUES
  -- Python
  ('c3000000-0000-0000-0000-000000000101', 'b2000000-0000-0000-0000-000000000001', 'Getting Started', 0, 'seed'),
  ('c3000000-0000-0000-0000-000000000102', 'b2000000-0000-0000-0000-000000000001', 'Control Flow', 1, 'seed'),
  ('c3000000-0000-0000-0000-000000000103', 'b2000000-0000-0000-0000-000000000001', 'Functions & Data', 2, 'seed'),
  -- Java
  ('c3000000-0000-0000-0000-000000000201', 'b2000000-0000-0000-0000-000000000002', 'Java Basics', 0, 'seed'),
  ('c3000000-0000-0000-0000-000000000202', 'b2000000-0000-0000-0000-000000000002', 'Control Flow & Methods', 1, 'seed'),
  ('c3000000-0000-0000-0000-000000000203', 'b2000000-0000-0000-0000-000000000002', 'Object-Oriented Java', 2, 'seed'),
  -- Core Data Structures
  ('c3000000-0000-0000-0000-000000000301', 'b2000000-0000-0000-0000-000000000003', 'Linear Structures', 0, 'seed'),
  ('c3000000-0000-0000-0000-000000000302', 'b2000000-0000-0000-0000-000000000003', 'Non-Linear Structures', 1, 'seed'),
  -- Algorithmic Thinking
  ('c3000000-0000-0000-0000-000000000401', 'b2000000-0000-0000-0000-000000000004', 'Measuring Cost', 0, 'seed'),
  ('c3000000-0000-0000-0000-000000000402', 'b2000000-0000-0000-0000-000000000004', 'Searching & Sorting', 1, 'seed'),
  ('c3000000-0000-0000-0000-000000000403', 'b2000000-0000-0000-0000-000000000004', 'Problem-Solving Patterns', 2, 'seed'),
  -- HTML & CSS
  ('c3000000-0000-0000-0000-000000000501', 'b2000000-0000-0000-0000-000000000005', 'HTML', 0, 'seed'),
  ('c3000000-0000-0000-0000-000000000502', 'b2000000-0000-0000-0000-000000000005', 'CSS', 1, 'seed'),
  -- JavaScript
  ('c3000000-0000-0000-0000-000000000601', 'b2000000-0000-0000-0000-000000000006', 'Language Basics', 0, 'seed'),
  ('c3000000-0000-0000-0000-000000000602', 'b2000000-0000-0000-0000-000000000006', 'Working with the DOM', 1, 'seed'),
  ('c3000000-0000-0000-0000-000000000603', 'b2000000-0000-0000-0000-000000000006', 'Modern JavaScript', 2, 'seed'),
  -- SQL
  ('c3000000-0000-0000-0000-000000000701', 'b2000000-0000-0000-0000-000000000007', 'Querying Data', 0, 'seed'),
  ('c3000000-0000-0000-0000-000000000702', 'b2000000-0000-0000-0000-000000000007', 'Aggregating & Combining', 1, 'seed');

-- ============================================================
-- Lessons
-- ============================================================

-- ---- Python :: Getting Started ----
INSERT INTO lessons (module_id, title, order_index, kind, estimated_minutes, body, created_by) VALUES
('c3000000-0000-0000-0000-000000000101', 'What is Python?', 0, 'reading', 8, $md$
# What is Python?

Python is a general-purpose programming language designed to be easy to read and quick to write. You can use it for web backends, data analysis, automation scripts, machine learning, and much more. Its popularity comes from a simple rule the community takes seriously: code is read far more often than it is written, so clarity wins.

## Your first program

A program is just a list of instructions. Here is the traditional first one:

```python
print("Hello, world!")
```

`print` is a built-in **function** — a named action you can call. The text in quotes is a **string**, a piece of text data. Running this line tells Python to display the message on the screen.

## How Python runs

Python is **interpreted**: you hand the interpreter your source file and it executes it line by line, top to bottom. There is no separate "compile" step you manage by hand, which makes the write-run-fix loop very fast.

## What's next

In the next lessons you will store data in variables, make decisions with `if`, and repeat work with loops. That trio — data, decisions, repetition — is the backbone of every program you will ever write.

---
**Go deeper (open references):** The official [Python Tutorial](https://docs.python.org/3/tutorial/) (Python Software Foundation) and the [Python article on Wikipedia](https://en.wikipedia.org/wiki/Python_(programming_language)) (CC-BY-SA).
$md$, 'seed'),

('c3000000-0000-0000-0000-000000000101', 'Variables and Data Types', 1, 'reading', 12, $md$
# Variables and Data Types

A **variable** is a name that points at a value. You create one with `=`:

```python
age = 21
name = "Dilshod"
price = 19.99
is_student = True
```

Notice you never declared a type. Python figures out the type from the value — this is called **dynamic typing**.

## The core built-in types

- `int` — whole numbers, e.g. `21`
- `float` — numbers with a decimal point, e.g. `19.99`
- `str` — text, e.g. `"Dilshod"`
- `bool` — `True` or `False`

You can check a value's type with the `type()` function:

```python
print(type(price))   # <class 'float'>
```

## Names that explain themselves

A good variable name tells the reader what the value means. `total_price` beats `tp`; `is_active` beats `flag`. Use lowercase words joined by underscores — that is the Python convention (`snake_case`).

## Reassigning

A variable can point at a new value at any time, even a value of a different type:

```python
count = 5
count = count + 1   # now 6
count = "done"      # now a string — valid, but usually a sign of confused code
```

---
**Go deeper (open references):** [Python data types reference](https://docs.python.org/3/library/stdtypes.html) (Python Software Foundation).
$md$, 'seed'),

('c3000000-0000-0000-0000-000000000101', 'Working with Strings', 2, 'reading', 10, $md$
# Working with Strings

Text is everywhere in programs — names, messages, file contents. In Python text is the `str` type.

## Creating and joining

```python
first = "Ada"
last = "Lovelace"
full = first + " " + last     # "Ada Lovelace"
```

The cleanest way to build a string from values is an **f-string**: put an `f` before the quotes and drop expressions inside `{}`:

```python
age = 36
message = f"{full} is {age} years old"
```

## Useful operations

```python
text = "  Hello World  "
text.strip()        # "Hello World"  (removes surrounding spaces)
text.lower()        # "  hello world  "
text.replace("o", "0")
len("Hello")        # 5
"World" in text     # True
```

Strings are **immutable**: methods like `.lower()` return a *new* string rather than changing the original.

## Indexing

Each character has a position, starting at `0`:

```python
word = "Python"
word[0]    # "P"
word[-1]   # "n"  (negative counts from the end)
word[0:3]  # "Pyt" (a slice: start included, end excluded)
```

---
**Go deeper (open references):** [Text sequence type — str](https://docs.python.org/3/library/stdtypes.html#text-sequence-type-str) (Python Software Foundation).
$md$, 'seed');

-- ---- Python :: Control Flow ----
INSERT INTO lessons (module_id, title, order_index, kind, estimated_minutes, body, created_by) VALUES
('c3000000-0000-0000-0000-000000000102', 'Making Decisions with if', 0, 'reading', 10, $md$
# Making Decisions with if

Programs become useful when they can choose between actions. The `if` statement runs a block only when a condition is true.

```python
temperature = 30

if temperature > 25:
    print("It's warm — wear a t-shirt.")
elif temperature > 10:
    print("Mild day.")
else:
    print("Bring a jacket.")
```

## Indentation is the syntax

Unlike many languages, Python has no curly braces around blocks. The **indentation** (four spaces by convention) is what groups statements together. Everything indented under the `if` runs when the condition holds.

## Comparison and boolean operators

```python
a == b   # equal
a != b   # not equal
a < b, a <= b, a > b, a >= b

x > 0 and x < 10   # both must be true
x < 0 or x > 100   # at least one
not done           # flips a boolean
```

A condition always reduces to `True` or `False`. Empty values (`0`, `""`, empty lists) count as false — handy, but be explicit when clarity matters.

---
**Go deeper (open references):** [More control flow tools](https://docs.python.org/3/tutorial/controlflow.html) (Python Software Foundation).
$md$, 'seed'),

('c3000000-0000-0000-0000-000000000102', 'Repeating Work with Loops', 1, 'reading', 12, $md$
# Repeating Work with Loops

When you need to do something many times, you use a loop instead of copying code.

## for loops

A `for` loop walks through the items of a sequence:

```python
for name in ["Ada", "Alan", "Grace"]:
    print(f"Hello, {name}")
```

To repeat a fixed number of times, use `range`:

```python
for i in range(5):     # 0, 1, 2, 3, 4
    print(i)
```

## while loops

A `while` loop runs as long as a condition stays true:

```python
count = 3
while count > 0:
    print(count)
    count = count - 1
print("Liftoff!")
```

Make sure something inside the loop eventually makes the condition false, or it will run forever.

## break and continue

- `break` exits the loop immediately.
- `continue` skips to the next iteration.

```python
for n in range(10):
    if n == 5:
        break          # stop entirely at 5
    if n % 2 == 0:
        continue       # skip even numbers
    print(n)           # prints 1, 3
```

---
**Go deeper (open references):** [The for statement](https://docs.python.org/3/tutorial/controlflow.html#for-statements) (Python Software Foundation).
$md$, 'seed');

-- ---- Python :: Functions & Data ----
INSERT INTO lessons (module_id, title, order_index, kind, estimated_minutes, body, created_by) VALUES
('c3000000-0000-0000-0000-000000000103', 'Defining Functions', 0, 'reading', 12, $md$
# Defining Functions

A **function** packages a piece of work behind a name so you can reuse it. Define one with `def`:

```python
def greet(name):
    return f"Hello, {name}!"

message = greet("Ada")   # "Hello, Ada!"
```

- `name` is a **parameter** — an input the function expects.
- `return` hands a value back to whoever called the function.

## Default and keyword arguments

```python
def power(base, exponent=2):
    return base ** exponent

power(5)            # 25  (uses default exponent)
power(2, 10)        # 1024
power(base=3, exponent=4)
```

## Why functions matter

Functions let you name an idea once and use it everywhere. They keep programs short, make bugs easier to isolate, and let you read code at a high level ("validate, then save, then notify") without drowning in detail. A good function does one clear thing.

---
**Go deeper (open references):** [Defining functions](https://docs.python.org/3/tutorial/controlflow.html#defining-functions) (Python Software Foundation).
$md$, 'seed'),

('c3000000-0000-0000-0000-000000000103', 'Lists and Dictionaries', 1, 'reading', 14, $md$
# Lists and Dictionaries

Two collection types do most of the heavy lifting in Python.

## Lists — ordered sequences

```python
scores = [90, 85, 72]
scores.append(100)     # add to the end
scores[0]              # 90
scores[-1]             # 100
len(scores)            # 4

for s in scores:
    print(s)
```

Lists are **mutable**: you can change them in place. They keep their order, allow duplicates, and are ideal when position matters.

## Dictionaries — key/value pairs

A dictionary maps **keys** to **values**, like a real dictionary maps words to definitions:

```python
student = {"name": "Ada", "age": 36, "active": True}
student["name"]            # "Ada"
student["age"] = 37        # update
student["email"] = "a@x.io"  # add a new key

for key, value in student.items():
    print(key, "=", value)
```

Use a dictionary when you want to look something up *by name* rather than by position. Lookups are very fast regardless of size — a property you will understand fully when you reach hash tables.

---
**Go deeper (open references):** [Data structures — lists & dicts](https://docs.python.org/3/tutorial/datastructures.html) (Python Software Foundation).
$md$, 'seed');

-- ---- Java :: Java Basics ----
INSERT INTO lessons (module_id, title, order_index, kind, estimated_minutes, body, created_by) VALUES
('c3000000-0000-0000-0000-000000000201', 'The Java Platform', 0, 'reading', 10, $md$
# The Java Platform

Java is a statically typed, object-oriented language famous for the slogan "write once, run anywhere." That promise comes from how Java runs.

## Source → bytecode → JVM

1. You write `.java` source files.
2. The **compiler** (`javac`) turns them into platform-neutral **bytecode** (`.class` files).
3. The **Java Virtual Machine (JVM)** executes that bytecode on any operating system that has a JVM.

Because the bytecode is the same everywhere, the same compiled program runs on Windows, macOS, and Linux without changes.

## Your first class

Every Java program lives inside a class. Execution begins at the `main` method:

```java
public class Hello {
    public static void main(String[] args) {
        System.out.println("Hello, world!");
    }
}
```

Save it as `Hello.java`, then:

```
javac Hello.java   # compile -> Hello.class
java Hello         # run
```

Java is more verbose than Python, but that structure pays off in large codebases where the compiler catches mistakes before the program ever runs.

---
**Go deeper (open references):** [The Java Tutorials](https://dev.java/learn/) (Oracle) and [Java (programming language)](https://en.wikipedia.org/wiki/Java_(programming_language)) on Wikipedia (CC-BY-SA).
$md$, 'seed'),

('c3000000-0000-0000-0000-000000000201', 'Variables and Types', 1, 'reading', 12, $md$
# Variables and Types

Java is **statically typed**: every variable has a type fixed at compile time, and you must declare it.

```java
int age = 21;
double price = 19.99;
boolean isStudent = true;
char grade = 'A';
String name = "Dilshod";
```

## Primitives vs. objects

- **Primitive types** (`int`, `double`, `boolean`, `char`, `long`, ...) hold raw values directly and are fast.
- **Reference types** (like `String` and any class) hold a reference to an object.

`String` is capitalized because it is a class, not a primitive.

## var for local inference

Since Java 10 you can let the compiler infer a local variable's type with `var` — the variable is still statically typed, you just write less:

```java
var total = 100;          // inferred as int
var greeting = "Hello";   // inferred as String
```

## Constants

Use `final` for a value that must never change:

```java
final double PI = 3.14159;
```

The compiler will reject any attempt to reassign a `final` variable — a guarantee you cannot get in dynamically typed languages.

---
**Go deeper (open references):** [Primitive data types](https://dev.java/learn/language-basics/) (Oracle).
$md$, 'seed'),

('c3000000-0000-0000-0000-000000000201', 'Operators and Expressions', 2, 'reading', 10, $md$
# Operators and Expressions

An **expression** is anything that produces a value. Operators combine values into expressions.

## Arithmetic

```java
int sum  = 7 + 3;     // 10
int diff = 7 - 3;     // 4
int prod = 7 * 3;     // 21
int quot = 7 / 3;     // 2  (integer division drops the remainder!)
int rem  = 7 % 3;     // 1  (modulo: the remainder)
double d = 7.0 / 3;   // 2.333... (one double makes it floating-point)
```

The integer-division surprise (`7 / 3 == 2`) trips up beginners constantly. If you want a decimal result, make at least one operand a `double`.

## Comparison and logic

```java
a == b      // equal  (for objects, compares references — use .equals for content)
a != b
a < b, a <= b, a > b, a >= b

x > 0 && x < 10   // logical AND
x < 0 || x > 100  // logical OR
!done             // NOT
```

## A note on comparing strings

For objects, `==` checks whether two references point at the *same* object. To compare text content, use `.equals`:

```java
"hi".equals(name)   // correct content comparison
```

---
**Go deeper (open references):** [Operators](https://dev.java/learn/language-basics/operators/) (Oracle).
$md$, 'seed');

-- ---- Java :: Control Flow & Methods ----
INSERT INTO lessons (module_id, title, order_index, kind, estimated_minutes, body, created_by) VALUES
('c3000000-0000-0000-0000-000000000202', 'Conditionals and Loops', 0, 'reading', 12, $md$
# Conditionals and Loops

Java groups blocks with curly braces `{ }` and ends statements with semicolons.

## if / else if / else

```java
int temp = 30;
if (temp > 25) {
    System.out.println("Warm");
} else if (temp > 10) {
    System.out.println("Mild");
} else {
    System.out.println("Cold");
}
```

## Loops

A `for` loop with a counter:

```java
for (int i = 0; i < 5; i++) {
    System.out.println(i);   // 0..4
}
```

An enhanced `for` loop over a collection or array:

```java
String[] names = {"Ada", "Alan", "Grace"};
for (String n : names) {
    System.out.println(n);
}
```

A `while` loop:

```java
int count = 3;
while (count > 0) {
    System.out.println(count);
    count--;
}
```

`break` leaves the loop; `continue` skips to the next iteration — exactly as in most C-family languages.

---
**Go deeper (open references):** [Control flow statements](https://dev.java/learn/language-basics/controlling-flow/) (Oracle).
$md$, 'seed'),

('c3000000-0000-0000-0000-000000000202', 'Writing Methods', 1, 'reading', 12, $md$
# Writing Methods

A **method** is Java's word for a function that belongs to a class. It has a return type, a name, and a parameter list.

```java
public class MathUtil {
    // returns an int, takes two ints
    static int add(int a, int b) {
        return a + b;
    }

    // returns nothing -> void
    static void greet(String name) {
        System.out.println("Hello, " + name);
    }

    public static void main(String[] args) {
        int sum = add(3, 4);   // 7
        greet("Ada");
    }
}
```

## Reading a signature

`static int add(int a, int b)`:

- `static` — belongs to the class itself (more on this when we cover objects).
- `int` — the **return type**; `void` means it returns nothing.
- `add` — the name.
- `(int a, int b)` — typed **parameters**.

## Overloading

Java lets several methods share a name as long as their parameter lists differ. The compiler picks the right one by the arguments you pass:

```java
static int max(int a, int b) { ... }
static double max(double a, double b) { ... }
```

---
**Go deeper (open references):** [Defining methods](https://dev.java/learn/classes-objects/methods/) (Oracle).
$md$, 'seed');

-- ---- Java :: Object-Oriented Java ----
INSERT INTO lessons (module_id, title, order_index, kind, estimated_minutes, body, created_by) VALUES
('c3000000-0000-0000-0000-000000000203', 'Classes and Objects', 0, 'reading', 14, $md$
# Classes and Objects

Object-oriented programming organizes code around **objects** — bundles of data and the behavior that operates on it. A **class** is the blueprint; an **object** is one thing built from it.

```java
public class Student {
    // fields (state)
    String name;
    int age;

    // constructor: builds a Student
    Student(String name, int age) {
        this.name = name;
        this.age = age;
    }

    // method (behavior)
    String describe() {
        return name + " (" + age + ")";
    }
}
```

Create objects with `new`:

```java
Student ada = new Student("Ada", 36);
System.out.println(ada.describe());   // Ada (36)
```

## this and encapsulation

`this.name` refers to the object's own field, distinguishing it from the parameter `name`. Real code usually makes fields `private` and exposes controlled access through methods (getters/setters) — this is **encapsulation**, hiding internal details so they can change safely.

```java
private int age;
public int getAge() { return age; }
```

---
**Go deeper (open references):** [Classes and objects](https://dev.java/learn/classes-objects/) (Oracle).
$md$, 'seed'),

('c3000000-0000-0000-0000-000000000203', 'Inheritance and Interfaces', 1, 'reading', 14, $md$
# Inheritance and Interfaces

Two mechanisms let Java classes share and promise behavior.

## Inheritance

A class can `extend` another, inheriting its fields and methods and adding or overriding its own:

```java
class Animal {
    String name;
    Animal(String name) { this.name = name; }
    String speak() { return "..."; }
}

class Dog extends Animal {
    Dog(String name) { super(name); }   // call the parent constructor
    @Override
    String speak() { return "Woof"; }
}
```

`super` calls up to the parent; `@Override` tells the compiler you intend to replace a parent method (and lets it catch typos).

## Interfaces

An **interface** is a contract: a list of methods a class promises to provide, with no implementation of its own.

```java
interface Shape {
    double area();
}

class Circle implements Shape {
    double radius;
    Circle(double r) { this.radius = r; }
    public double area() { return Math.PI * radius * radius; }
}
```

Any code that needs "something with an area" can accept a `Shape`, and every implementing class plugs in. This is **polymorphism** — programming to a contract instead of a concrete type.

---
**Go deeper (open references):** [Interfaces and inheritance](https://dev.java/learn/inheritance/) (Oracle).
$md$, 'seed');

-- ---- Core Data Structures :: Linear Structures ----
INSERT INTO lessons (module_id, title, order_index, kind, estimated_minutes, body, created_by) VALUES
('c3000000-0000-0000-0000-000000000301', 'Arrays and Dynamic Arrays', 0, 'reading', 14, $md$
# Arrays and Dynamic Arrays

An **array** is a block of memory holding a fixed number of elements of the same type, laid out one after another. Because the elements are contiguous, the computer can jump straight to any position by arithmetic.

## What's fast, what's slow

- **Access by index** — O(1). `arr[i]` is a single calculation: start address + i × element size.
- **Search for a value** — O(n). You may have to look at every element.
- **Insert/remove in the middle** — O(n). Everything after the spot must shift.

## Dynamic arrays

A fixed array can't grow. A **dynamic array** (Python's `list`, Java's `ArrayList`, C++'s `vector`) wraps an array and resizes automatically. When it runs out of room it allocates a bigger array (typically double) and copies the elements over.

That occasional copy is O(n), but it happens rarely enough that appending is **O(1) amortized** — averaged over many appends, each is effectively constant time.

```python
nums = []          # dynamic array
nums.append(10)    # O(1) amortized
nums[0]            # O(1)
```

Reach for an array/list when you mostly read by index and append at the end — which is most of the time.

---
**Go deeper (open references):** [Array data structure](https://en.wikipedia.org/wiki/Array_(data_structure)) and [Dynamic array](https://en.wikipedia.org/wiki/Dynamic_array) on Wikipedia (CC-BY-SA).
$md$, 'seed'),

('c3000000-0000-0000-0000-000000000301', 'Linked Lists', 1, 'reading', 12, $md$
# Linked Lists

A **linked list** stores each element in its own **node**, and each node holds a pointer to the next one. The elements are *not* contiguous in memory — they are chained.

```
[10|·]→[20|·]→[30|null]
```

## Trade-offs vs. arrays

- **Insert/remove at a known node** — O(1). Just rewire two pointers; nothing shifts.
- **Access by index** — O(n). There is no arithmetic shortcut; you must walk the chain.
- **Extra memory** — each node stores a pointer alongside its value.

## A node in code

```python
class Node:
    def __init__(self, value):
        self.value = value
        self.next = None

a = Node(10)
a.next = Node(20)          # 10 -> 20
```

## When to use one

Linked lists shine when you insert and delete frequently at positions you already hold a reference to, and rarely need random access. In practice, dynamic arrays win for most everyday work because of cache-friendly contiguous memory — but linked lists are the foundation of stacks, queues, and many other structures.

---
**Go deeper (open references):** [Linked list](https://en.wikipedia.org/wiki/Linked_list) on Wikipedia (CC-BY-SA).
$md$, 'seed'),

('c3000000-0000-0000-0000-000000000301', 'Stacks and Queues', 2, 'reading', 12, $md$
# Stacks and Queues

Stacks and queues are about *discipline*: they restrict how you add and remove items, and that restriction is exactly what makes them useful.

## Stack — Last In, First Out (LIFO)

Like a stack of plates: you add to the top and remove from the top.

- `push` — add to the top
- `pop` — remove from the top

Both are O(1). Stacks power undo features, the call stack that tracks function calls, and expression evaluation.

```python
stack = []
stack.append("a")   # push
stack.append("b")
stack.pop()         # "b"  (last in, first out)
```

## Queue — First In, First Out (FIFO)

Like a line at a shop: the first to arrive is the first served.

- `enqueue` — add to the back
- `dequeue` — remove from the front

Queues model task schedulers, print jobs, and the breadth-first traversal you will meet with graphs. In Python use `collections.deque` for O(1) removal from the front:

```python
from collections import deque
q = deque()
q.append("a")       # enqueue
q.append("b")
q.popleft()         # "a"  (first in, first out)
```

---
**Go deeper (open references):** [Stack](https://en.wikipedia.org/wiki/Stack_(abstract_data_type)) and [Queue](https://en.wikipedia.org/wiki/Queue_(abstract_data_type)) on Wikipedia (CC-BY-SA).
$md$, 'seed');

-- ---- Core Data Structures :: Non-Linear Structures ----
INSERT INTO lessons (module_id, title, order_index, kind, estimated_minutes, body, created_by) VALUES
('c3000000-0000-0000-0000-000000000302', 'Hash Tables', 0, 'reading', 14, $md$
# Hash Tables

A **hash table** stores key/value pairs and finds any value by its key in **O(1) average time**. It is the structure behind Python's `dict`, Java's `HashMap`, and JavaScript's `Map` and plain objects.

## The idea

A **hash function** turns a key into a number, which is reduced to a slot (bucket) in an underlying array. To store `("age", 21)`, the table hashes `"age"`, lands on a bucket, and puts the pair there. To look it up later, it hashes `"age"` again and goes straight to that bucket — no scanning.

## Collisions

Two different keys can hash to the same bucket. Tables handle this with **chaining** (each bucket holds a small list) or **open addressing** (probe for the next free slot). With a good hash function and enough buckets, collisions are rare, so operations stay near O(1).

## Costs

- Insert, lookup, delete — **O(1) average**, O(n) worst case (everything collides).
- No order guarantee in the classic version (though Python dicts preserve insertion order as an implementation detail).

```python
ages = {}
ages["Ada"] = 36     # insert
ages["Ada"]          # lookup, O(1) average
"Ada" in ages        # True
```

Whenever you think "look this up by name/id," a hash table is usually the right answer.

---
**Go deeper (open references):** [Hash table](https://en.wikipedia.org/wiki/Hash_table) on Wikipedia (CC-BY-SA).
$md$, 'seed'),

('c3000000-0000-0000-0000-000000000302', 'Trees and Binary Search Trees', 1, 'reading', 14, $md$
# Trees and Binary Search Trees

A **tree** is a hierarchy of nodes: one **root** at the top, each node holding children below it, with no cycles. File systems, HTML documents, and org charts are all trees.

## Binary trees

In a **binary tree** each node has at most two children, called *left* and *right*.

```python
class TreeNode:
    def __init__(self, value):
        self.value = value
        self.left = None
        self.right = None
```

## Binary Search Trees (BST)

A **BST** keeps an ordering rule at every node:

> everything in the left subtree is smaller; everything in the right subtree is larger.

That invariant lets you search like a guessing game — compare, then go left or right, halving the remaining nodes each step:

```
        8
      /   \
     3     10
    / \      \
   1   6      14
```

Searching for `6`: start at 8 (go left), reach 3 (go right), find 6.

- Search / insert / delete — **O(log n)** when the tree stays balanced, O(n) if it degenerates into a chain.

Balanced variants (AVL, red-black trees) add rotations to guarantee the O(log n) shape. They underpin ordered maps and database indexes.

---
**Go deeper (open references):** [Binary search tree](https://en.wikipedia.org/wiki/Binary_search_tree) on Wikipedia (CC-BY-SA).
$md$, 'seed'),

('c3000000-0000-0000-0000-000000000302', 'Graphs', 2, 'reading', 12, $md$
# Graphs

A **graph** is a set of **vertices** (nodes) connected by **edges**. Unlike a tree it can have cycles and any pattern of connections. Social networks, maps, and dependency systems are all graphs.

## Vocabulary

- **Directed** vs **undirected** — do edges have a one-way arrow (follows) or go both ways (friendship)?
- **Weighted** — edges can carry a number (distance, cost).
- **Adjacency** — two vertices are adjacent if an edge joins them.

## Representing a graph

The common, memory-efficient choice is an **adjacency list** — for each vertex, the list of its neighbors:

```python
graph = {
    "A": ["B", "C"],
    "B": ["A", "D"],
    "C": ["A"],
    "D": ["B"],
}
```

## Traversal

Two ways to visit every reachable vertex:

- **Breadth-First Search (BFS)** uses a *queue*, exploring level by level. It finds the shortest path in an unweighted graph.
- **Depth-First Search (DFS)** uses a *stack* (often the call stack via recursion), diving deep before backtracking.

These two traversals are the basis for pathfinding, cycle detection, and much more.

---
**Go deeper (open references):** [Graph (abstract data type)](https://en.wikipedia.org/wiki/Graph_(abstract_data_type)) on Wikipedia (CC-BY-SA).
$md$, 'seed');

-- ---- Algorithmic Thinking :: Measuring Cost ----
INSERT INTO lessons (module_id, title, order_index, kind, estimated_minutes, body, created_by) VALUES
('c3000000-0000-0000-0000-000000000401', 'Big-O Notation', 0, 'reading', 14, $md$
# Big-O Notation

How do you compare two solutions without running them on every possible input? You describe how their cost **grows** as the input gets large. That is what **Big-O notation** captures.

## Reading Big-O

Big-O describes the *upper bound* on growth, ignoring constants and small terms. We care about the shape, not the exact count.

| Notation | Name | Example |
|----------|------|---------|
| O(1) | constant | array access by index |
| O(log n) | logarithmic | binary search |
| O(n) | linear | scanning a list |
| O(n log n) | linearithmic | good sorting algorithms |
| O(n²) | quadratic | nested loops over the same data |
| O(2ⁿ) | exponential | trying every subset |

## Why constants drop

If one algorithm does `3n + 10` steps and another does `n²`, for small `n` the second might win — but as `n` grows, `n²` overwhelms everything. Big-O focuses on that long-run behavior, so `3n + 10` is simply **O(n)**.

## Spotting it in code

```python
# O(n): one pass
for x in items:
    print(x)

# O(n^2): a loop inside a loop over the same data
for a in items:
    for b in items:
        print(a, b)
```

Nested loops over the same input are the classic warning sign of O(n²). Recognizing these patterns lets you predict whether code will scale before you ship it.

---
**Go deeper (open references):** [Big O notation](https://en.wikipedia.org/wiki/Big_O_notation) and [Time complexity](https://en.wikipedia.org/wiki/Time_complexity) on Wikipedia (CC-BY-SA).
$md$, 'seed');

-- ---- Algorithmic Thinking :: Searching & Sorting ----
INSERT INTO lessons (module_id, title, order_index, kind, estimated_minutes, body, created_by) VALUES
('c3000000-0000-0000-0000-000000000402', 'Binary Search', 0, 'reading', 12, $md$
# Binary Search

**Binary search** finds a target in a **sorted** list in O(log n) time by repeatedly halving the search range — the same way you find a word in a dictionary by opening to the middle.

## The algorithm

1. Look at the middle element.
2. If it equals the target, done.
3. If the target is smaller, repeat on the left half.
4. If larger, repeat on the right half.

Each step throws away half of what's left, so even a million items takes only about 20 comparisons.

```python
def binary_search(arr, target):
    lo, hi = 0, len(arr) - 1
    while lo <= hi:
        mid = (lo + hi) // 2
        if arr[mid] == target:
            return mid
        elif arr[mid] < target:
            lo = mid + 1
        else:
            hi = mid - 1
    return -1   # not found
```

## The catch

Binary search only works on **sorted** data. If your data isn't sorted, you either sort it first (O(n log n)) or use a different structure like a hash table. The "is it sorted?" question is the first thing to ask whenever a search feels slow.

---
**Go deeper (open references):** [Binary search algorithm](https://en.wikipedia.org/wiki/Binary_search_algorithm) on Wikipedia (CC-BY-SA).
$md$, 'seed'),

('c3000000-0000-0000-0000-000000000402', 'Sorting Algorithms', 1, 'reading', 14, $md$
# Sorting Algorithms

Sorting puts elements in order, and it is the warm-up problem for learning algorithm design because so many strategies exist.

## Simple (but slow) sorts — O(n²)

**Bubble sort** and **insertion sort** compare neighbors and swap them into place. Easy to understand, fine for tiny inputs, but they don't scale because of the nested passes.

```python
# insertion sort: grow a sorted prefix one element at a time
def insertion_sort(a):
    for i in range(1, len(a)):
        key = a[i]
        j = i - 1
        while j >= 0 and a[j] > key:
            a[j + 1] = a[j]
            j -= 1
        a[j + 1] = key
    return a
```

## Efficient sorts — O(n log n)

**Merge sort** splits the list in half, sorts each half, and merges them back in order — a clean example of **divide and conquer**. **Quicksort** partitions around a pivot and recurses. Both reach O(n log n), which is the best a comparison-based sort can do.

## In practice

You rarely write your own sort — language libraries use highly tuned hybrids (Python's Timsort, Java's dual-pivot quicksort). Reach for the built-in:

```python
nums = [5, 2, 9, 1]
nums.sort()                 # in place
ordered = sorted(nums)      # returns a new list
nums.sort(key=len)          # sort by a custom rule
```

Knowing the algorithms still matters: it tells you *why* `sort()` costs O(n log n) and when a clever approach can beat a general one.

---
**Go deeper (open references):** [Sorting algorithm](https://en.wikipedia.org/wiki/Sorting_algorithm) and [Merge sort](https://en.wikipedia.org/wiki/Merge_sort) on Wikipedia (CC-BY-SA).
$md$, 'seed');

-- ---- Algorithmic Thinking :: Problem-Solving Patterns ----
INSERT INTO lessons (module_id, title, order_index, kind, estimated_minutes, body, created_by) VALUES
('c3000000-0000-0000-0000-000000000403', 'Recursion', 0, 'reading', 12, $md$
# Recursion

**Recursion** is when a function solves a problem by calling itself on a smaller version of the same problem. Every recursive solution needs two parts:

1. A **base case** that stops the recursion.
2. A **recursive case** that moves toward the base case.

```python
def factorial(n):
    if n <= 1:          # base case
        return 1
    return n * factorial(n - 1)   # recursive case
```

`factorial(4)` becomes `4 * factorial(3)` → `4 * 3 * factorial(2)` → ... → `24`.

## Thinking recursively

Instead of "how do I do all the work," ask: "if I trust the function to handle the smaller case, how do I combine that with one more step?" This mindset makes tree and graph problems natural, because their structure is itself recursive (a subtree is just a smaller tree).

## The cost

Each call uses a frame on the **call stack**. Too-deep recursion can overflow it, and naive recursion can repeat work — `fib(n)` recomputing the same values is the classic example, fixed by **memoization** (caching results). Any recursion can be rewritten as a loop, but for the right problems recursion is far clearer.

---
**Go deeper (open references):** [Recursion (computer science)](https://en.wikipedia.org/wiki/Recursion_(computer_science)) on Wikipedia (CC-BY-SA).
$md$, 'seed'),

('c3000000-0000-0000-0000-000000000403', 'Two Pointers and Sliding Window', 1, 'reading', 12, $md$
# Two Pointers and Sliding Window

Two related patterns turn many O(n²) brute-force solutions into O(n) ones. They show up constantly in array and string problems.

## Two pointers

Keep two indices that move toward each other or in the same direction. Classic use: check whether a **sorted** array has a pair summing to a target.

```python
def has_pair(arr, target):     # arr is sorted
    lo, hi = 0, len(arr) - 1
    while lo < hi:
        s = arr[lo] + arr[hi]
        if s == target:
            return True
        elif s < target:
            lo += 1            # need a bigger sum
        else:
            hi -= 1            # need a smaller sum
    return False
```

Instead of checking every pair (O(n²)), each element is visited once (O(n)).

## Sliding window

Maintain a "window" `[start, end]` over a sequence and slide it, expanding and shrinking to satisfy a condition — ideal for "longest/shortest subarray" questions.

```python
def max_sum_window(arr, k):
    window = sum(arr[:k])
    best = window
    for end in range(k, len(arr)):
        window += arr[end] - arr[end - k]   # add new, drop old
        best = max(best, window)
    return best
```

Recognizing when a problem fits these patterns is one of the highest-value skills for technical interviews and real performance work alike.

---
**Go deeper (open references):** see array-algorithm references such as [Two-pointer technique discussions](https://en.wikipedia.org/wiki/Two_pointers_technique) on Wikipedia (CC-BY-SA).
$md$, 'seed');

-- ---- HTML & CSS :: HTML ----
INSERT INTO lessons (module_id, title, order_index, kind, estimated_minutes, body, created_by) VALUES
('c3000000-0000-0000-0000-000000000501', 'Structure of a Web Page', 0, 'reading', 10, $md$
# Structure of a Web Page

**HTML** (HyperText Markup Language) describes the *structure and meaning* of a page. You write content and wrap it in **tags** that say what each part is.

## A minimal document

```html
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8" />
    <title>My First Page</title>
  </head>
  <body>
    <h1>Hello, web!</h1>
    <p>This is a paragraph of text.</p>
  </body>
</html>
```

- `<!DOCTYPE html>` declares the document as modern HTML.
- `<head>` holds metadata the visitor doesn't see directly (title, character set, links to styles).
- `<body>` holds everything visible.

## Elements, tags, attributes

An **element** is usually an opening tag, content, and a closing tag: `<p>Hello</p>`. **Attributes** add information inside the opening tag: `<html lang="en">` sets the language; `<a href="...">` sets a link target.

## Semantics matter

Choosing the right element (`<h1>` for a main heading, `<nav>` for navigation, `<button>` for a button) helps search engines and screen readers understand your page. This is called writing **semantic HTML**, and it is the difference between a page that merely looks right and one that *is* right.

---
**Go deeper (open references):** [MDN: HTML basics](https://developer.mozilla.org/en-US/docs/Learn/Getting_started_with_the_web/HTML_basics) (Mozilla, CC-BY-SA).
$md$, 'seed'),

('c3000000-0000-0000-0000-000000000501', 'Common HTML Elements', 1, 'reading', 12, $md$
# Common HTML Elements

A handful of elements cover most of what you build.

## Text

```html
<h1>Page title</h1>
<h2>Section heading</h2>
<p>A paragraph.</p>
<strong>Important</strong> and <em>emphasized</em> text.
```

Use headings in order (`h1` → `h2` → `h3`) to express a real outline, not just to make text big.

## Lists

```html
<ul>                 <!-- unordered -->
  <li>First</li>
  <li>Second</li>
</ul>

<ol>                 <!-- ordered/numbered -->
  <li>Step one</li>
  <li>Step two</li>
</ol>
```

## Links and images

```html
<a href="https://example.com">Visit example</a>
<img src="cat.jpg" alt="A sleeping cat" />
```

Always give images a meaningful `alt` description — it's read aloud to people using screen readers and shown if the image fails to load.

## Structure and forms

```html
<nav> ... site navigation ... </nav>
<main> ... main content ... </main>
<footer> ... footer ... </footer>

<form>
  <label>Email <input type="email" name="email" /></label>
  <button type="submit">Sign up</button>
</form>
```

Pairing each `<input>` with a `<label>` makes forms usable and accessible.

---
**Go deeper (open references):** [MDN: HTML element reference](https://developer.mozilla.org/en-US/docs/Web/HTML/Element) (Mozilla, CC-BY-SA).
$md$, 'seed');

-- ---- HTML & CSS :: CSS ----
INSERT INTO lessons (module_id, title, order_index, kind, estimated_minutes, body, created_by) VALUES
('c3000000-0000-0000-0000-000000000502', 'Selectors and the Box Model', 0, 'reading', 12, $md$
# Selectors and the Box Model

**CSS** (Cascading Style Sheets) controls how HTML looks. You write **rules** that select elements and apply styles.

## A rule

```css
p {
  color: #334155;
  font-size: 16px;
  line-height: 1.6;
}
```

`p` is the **selector**; the pairs inside `{ }` are **declarations** (property: value).

## Common selectors

```css
h1            { }   /* every <h1> */
.card         { }   /* every element with class="card" */
#main         { }   /* the element with id="main" */
nav a         { }   /* <a> inside <nav> */
button:hover  { }   /* a button while hovered */
```

Classes are the workhorse — reusable and not unique like ids.

## The box model

Every element is a box made of four layers, from inside out:

1. **content** — the text or image
2. **padding** — space inside the border
3. **border** — the edge
4. **margin** — space outside, between this box and others

```css
.card {
  padding: 16px;
  border: 1px solid #e2e8f0;
  margin: 12px;
  box-sizing: border-box;   /* width includes padding + border */
}
```

`box-sizing: border-box` is the sane default almost everyone applies, because it makes widths predictable.

---
**Go deeper (open references):** [MDN: The box model](https://developer.mozilla.org/en-US/docs/Learn/CSS/Building_blocks/The_box_model) (Mozilla, CC-BY-SA).
$md$, 'seed'),

('c3000000-0000-0000-0000-000000000502', 'Flexbox and Grid', 1, 'reading', 14, $md$
# Flexbox and Grid

Modern CSS layout is built on two complementary tools: **Flexbox** for one dimension and **Grid** for two.

## Flexbox — rows or columns

Turn a container into a flex container and its children line up along one axis:

```css
.toolbar {
  display: flex;
  gap: 12px;
  align-items: center;        /* vertical alignment */
  justify-content: space-between;  /* horizontal distribution */
}
```

Flexbox is perfect for navbars, button groups, and centering — anything that flows in a single line (or wraps).

## Grid — rows and columns together

Grid lays elements out in a true 2D grid:

```css
.gallery {
  display: grid;
  grid-template-columns: repeat(3, 1fr);   /* three equal columns */
  gap: 16px;
}
```

`1fr` means "one fraction of the free space," so the columns share width evenly and respond to the container size.

## Which to use

- One direction (a row of buttons, a vertical stack) → **Flexbox**.
- A real grid of cards or a page layout with rows *and* columns → **Grid**.

They combine freely: a Grid page often contains Flexbox components.

---
**Go deeper (open references):** [MDN: Flexbox](https://developer.mozilla.org/en-US/docs/Web/CSS/CSS_flexible_box_layout) and [MDN: CSS Grid](https://developer.mozilla.org/en-US/docs/Web/CSS/CSS_grid_layout) (Mozilla, CC-BY-SA).
$md$, 'seed');

-- ---- JavaScript :: Language Basics ----
INSERT INTO lessons (module_id, title, order_index, kind, estimated_minutes, body, created_by) VALUES
('c3000000-0000-0000-0000-000000000601', 'Variables, Types, and Operators', 0, 'reading', 12, $md$
# Variables, Types, and Operators

**JavaScript** runs in every web browser and, via Node.js, on servers too. It is dynamically typed like Python but uses C-style syntax.

## Declaring variables

```js
let count = 5;        // can be reassigned
const name = "Ada";   // cannot be reassigned
```

Prefer `const` by default and switch to `let` only when you truly need to reassign. Avoid the old `var` — its scoping rules cause subtle bugs.

## Types

```js
let n = 42;            // number (no separate int/float)
let s = "hello";       // string
let ok = true;         // boolean
let nothing = null;    // intentional "no value"
let undef;             // undefined: declared but unset
let list = [1, 2, 3];  // array (object)
let user = { name: "Ada", age: 36 };  // object
```

## Operators and a famous gotcha

```js
1 + 1          // 2
"a" + "b"      // "ab"
10 % 3         // 1

1 == "1"       // true  — loose equality coerces types (avoid!)
1 === "1"      // false — strict equality, no coercion (use this)
```

Always use `===` and `!==`. The coercing `==` is a well-known source of bugs.

## Template literals

```js
const greeting = `Hello, ${name}! You have ${count} messages.`;
```

Backticks let you embed expressions with `${ }`, like Python's f-strings.

---
**Go deeper (open references):** [MDN: JavaScript first steps](https://developer.mozilla.org/en-US/docs/Learn/JavaScript/First_steps) (Mozilla, CC-BY-SA).
$md$, 'seed'),

('c3000000-0000-0000-0000-000000000601', 'Functions and Scope', 1, 'reading', 12, $md$
# Functions and Scope

Functions are values in JavaScript — you can store them, pass them, and return them. That flexibility is central to the language.

## Three ways to write one

```js
// declaration
function add(a, b) {
  return a + b;
}

// expression assigned to a const
const multiply = function (a, b) {
  return a * b;
};

// arrow function (concise, very common)
const square = (x) => x * x;
```

Arrow functions with a single expression return it automatically — no `return` needed.

## Scope

A variable declared with `let`/`const` lives only inside the block `{ }` where it's defined. Inner functions can read variables from the enclosing scope:

```js
function counter() {
  let n = 0;                 // "captured" by the inner function
  return () => { n += 1; return n; };
}
const next = counter();
next();  // 1
next();  // 2
```

The returned function "remembers" `n` even after `counter` finished. This is a **closure** — a function bundled with the variables it captured. Closures power callbacks, event handlers, and module patterns throughout JavaScript.

---
**Go deeper (open references):** [MDN: Functions](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide/Functions) and [Closures](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Closures) (Mozilla, CC-BY-SA).
$md$, 'seed');

-- ---- JavaScript :: Working with the DOM ----
INSERT INTO lessons (module_id, title, order_index, kind, estimated_minutes, body, created_by) VALUES
('c3000000-0000-0000-0000-000000000602', 'Selecting and Changing Elements', 0, 'reading', 12, $md$
# Selecting and Changing Elements

The **DOM** (Document Object Model) is the browser's live, tree-shaped representation of your HTML. JavaScript can read and change it, and the page updates instantly.

## Selecting elements

```js
const title = document.querySelector("h1");        // first match
const items = document.querySelectorAll(".item");  // all matches
const box   = document.getElementById("main");
```

`querySelector` takes any CSS selector, so the skills from styling transfer directly.

## Reading and changing content

```js
title.textContent = "Updated heading";
box.innerHTML = "<strong>Bold</strong> content";
```

Prefer `textContent` for plain text — it's safer because it won't interpret HTML (which protects against injection attacks).

## Styling and attributes

```js
box.classList.add("active");
box.classList.toggle("open");
box.style.color = "tomato";
const link = document.querySelector("a");
link.setAttribute("href", "https://example.com");
```

Adding and removing classes (rather than setting inline styles directly) keeps the look in CSS where it belongs.

---
**Go deeper (open references):** [MDN: Manipulating documents](https://developer.mozilla.org/en-US/docs/Learn/JavaScript/Client-side_web_APIs/Manipulating_documents) (Mozilla, CC-BY-SA).
$md$, 'seed'),

('c3000000-0000-0000-0000-000000000602', 'Handling Events', 1, 'reading', 12, $md$
# Handling Events

Interactivity comes from **events** — clicks, key presses, form submissions — and the functions you attach to respond to them.

## addEventListener

```js
const button = document.querySelector("button");

button.addEventListener("click", () => {
  console.log("Button clicked!");
});
```

The first argument is the event name; the second is a **handler** that runs when it fires. Common events: `click`, `input`, `submit`, `keydown`, `mouseover`.

## The event object

Handlers receive an event object with details and controls:

```js
const form = document.querySelector("form");
form.addEventListener("submit", (event) => {
  event.preventDefault();   // stop the page from reloading
  const value = event.target.querySelector("input").value;
  console.log("Submitted:", value);
});
```

`event.preventDefault()` cancels the browser's default behavior (here, a full page reload), letting your JavaScript handle the form instead.

## A small interactive example

```js
let count = 0;
const label = document.querySelector("#count");
document.querySelector("#inc").addEventListener("click", () => {
  count += 1;
  label.textContent = count;
});
```

Select → listen → update the DOM. That three-step loop is the heart of every interactive page, and the idea every UI framework builds upon.

---
**Go deeper (open references):** [MDN: Introduction to events](https://developer.mozilla.org/en-US/docs/Learn/JavaScript/Building_blocks/Events) (Mozilla, CC-BY-SA).
$md$, 'seed');

-- ---- JavaScript :: Modern JavaScript ----
INSERT INTO lessons (module_id, title, order_index, kind, estimated_minutes, body, created_by) VALUES
('c3000000-0000-0000-0000-000000000603', 'Arrays and Iteration', 0, 'reading', 12, $md$
# Arrays and Iteration

Modern JavaScript leans on a small set of array methods that replace most manual loops and read like plain English.

## map, filter, reduce

```js
const nums = [1, 2, 3, 4];

const doubled = nums.map((n) => n * 2);        // [2, 4, 6, 8]
const evens   = nums.filter((n) => n % 2 === 0); // [2, 4]
const total   = nums.reduce((sum, n) => sum + n, 0); // 10
```

- `map` transforms every element, returning a new array of the same length.
- `filter` keeps only elements that pass a test.
- `reduce` collapses an array into a single value.

Each returns a new array (or value) without changing the original — favoring this **immutable** style makes code easier to reason about.

## Other everyday helpers

```js
nums.forEach((n) => console.log(n));   // side effects, no return
nums.find((n) => n > 2);               // 3 (first match)
nums.some((n) => n > 3);               // true
nums.every((n) => n > 0);              // true
```

## Destructuring and spread

```js
const [first, ...rest] = nums;   // first = 1, rest = [2,3,4]
const more = [...nums, 5, 6];    // copy and extend
const { name } = { name: "Ada", age: 36 };  // pull a field out
```

These features show up in almost every modern codebase, including React.

---
**Go deeper (open references):** [MDN: Array](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array) (Mozilla, CC-BY-SA).
$md$, 'seed'),

('c3000000-0000-0000-0000-000000000603', 'Promises and async/await', 1, 'reading', 14, $md$
# Promises and async/await

Web code constantly waits — for network requests, timers, files. JavaScript handles waiting **asynchronously**: it kicks off the work and keeps running, then reacts when the result arrives, instead of freezing the page.

## Promises

A **Promise** represents a value that will exist later. It either *resolves* with a value or *rejects* with an error:

```js
fetch("/api/v1/learning/tracks")
  .then((response) => response.json())
  .then((data) => console.log(data))
  .catch((error) => console.error(error));
```

## async / await

`async`/`await` lets you write asynchronous code that reads top-to-bottom like synchronous code:

```js
async function loadTracks() {
  try {
    const response = await fetch("/api/v1/learning/tracks");
    const data = await response.json();
    return data;
  } catch (error) {
    console.error("Failed to load:", error);
  }
}
```

`await` pauses *inside the function* until the promise settles, without blocking the rest of the page. Any function using `await` must be marked `async`, and calling an `async` function itself returns a promise.

## Why it matters

This model is how every modern app talks to its backend. The very API client in this project uses `async`/`await` and `fetch` to load your courses, exactly like the example above.

---
**Go deeper (open references):** [MDN: Using promises](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide/Using_promises) (Mozilla, CC-BY-SA).
$md$, 'seed');

-- ---- SQL :: Querying Data ----
INSERT INTO lessons (module_id, title, order_index, kind, estimated_minutes, body, created_by) VALUES
('c3000000-0000-0000-0000-000000000701', 'SELECT Basics', 0, 'reading', 10, $md$
# SELECT Basics

A **relational database** stores data in **tables** — rows and columns, like a spreadsheet with rules. **SQL** (Structured Query Language) is how you ask it questions. The most important query is `SELECT`.

## Reading columns

Imagine a `students` table:

| id | name  | age | country |
|----|-------|-----|---------|
| 1  | Ada   | 36  | UK      |
| 2  | Alan  | 41  | UK      |
| 3  | Grace | 29  | US      |

```sql
SELECT name, age FROM students;
```

This returns just the `name` and `age` columns for every row. To get every column, use `*`:

```sql
SELECT * FROM students;
```

## Aliases and computed columns

```sql
SELECT name AS student_name,
       age + 1 AS age_next_year
FROM students;
```

`AS` renames a column in the result, and you can compute new values right in the `SELECT`.

## A note on style

SQL keywords are case-insensitive (`select` works), but writing them in UPPERCASE is a widespread convention that makes queries easy to scan. End each statement with a semicolon.

---
**Go deeper (open references):** [SQL on Wikipedia](https://en.wikipedia.org/wiki/SQL) (CC-BY-SA) and the [PostgreSQL SELECT documentation](https://www.postgresql.org/docs/current/sql-select.html).
$md$, 'seed'),

('c3000000-0000-0000-0000-000000000701', 'Filtering with WHERE', 1, 'reading', 12, $md$
# Filtering with WHERE

`WHERE` keeps only the rows that match a condition — it's how you ask focused questions.

```sql
SELECT name, age FROM students
WHERE country = 'UK';
```

## Comparison and logic

```sql
WHERE age >= 30
WHERE age BETWEEN 25 AND 40
WHERE country = 'UK' AND age > 35
WHERE country = 'US' OR country = 'UK'
WHERE country IN ('US', 'UK', 'CA')
WHERE age <> 30           -- not equal
```

## Matching text with LIKE

```sql
WHERE name LIKE 'A%'      -- starts with A
WHERE name LIKE '%a'      -- ends with a
WHERE name LIKE '%ra%'    -- contains "ra"
```

`%` matches any run of characters; `_` matches exactly one.

## Handling missing values

A missing value is `NULL`, and it needs special operators — `= NULL` never works:

```sql
WHERE email IS NULL
WHERE email IS NOT NULL
```

`NULL` means "unknown," so any normal comparison with it yields unknown rather than true. Remembering that prevents a whole category of silent bugs.

---
**Go deeper (open references):** [PostgreSQL: WHERE / conditions](https://www.postgresql.org/docs/current/queries-table-expressions.html#QUERIES-WHERE) (PostgreSQL docs).
$md$, 'seed'),

('c3000000-0000-0000-0000-000000000701', 'Sorting and Limiting', 2, 'reading', 8, $md$
# Sorting and Limiting

Two clauses shape *which* rows you see and *in what order*.

## ORDER BY

```sql
SELECT name, age FROM students
ORDER BY age;            -- ascending by default

SELECT name, age FROM students
ORDER BY age DESC;       -- highest first

SELECT * FROM students
ORDER BY country ASC, age DESC;  -- by country, then age within each
```

You can sort by several columns; ties on the first are broken by the next.

## LIMIT and OFFSET

```sql
SELECT * FROM students
ORDER BY age DESC
LIMIT 3;                 -- top 3 oldest

SELECT * FROM students
ORDER BY age DESC
LIMIT 3 OFFSET 3;        -- the next 3 (rows 4-6)
```

`LIMIT` caps the number of rows; `OFFSET` skips some first. Together they power **pagination** — exactly how this app shows courses or leaderboard entries one page at a time. Always pair `LIMIT` with `ORDER BY`, or "the first 3 rows" is whatever order the database happens to return.

---
**Go deeper (open references):** [PostgreSQL: LIMIT and OFFSET](https://www.postgresql.org/docs/current/queries-limit.html) (PostgreSQL docs).
$md$, 'seed');

-- ---- SQL :: Aggregating & Combining ----
INSERT INTO lessons (module_id, title, order_index, kind, estimated_minutes, body, created_by) VALUES
('c3000000-0000-0000-0000-000000000702', 'Aggregate Functions and GROUP BY', 0, 'reading', 12, $md$
# Aggregate Functions and GROUP BY

**Aggregate functions** collapse many rows into a single summary value.

```sql
SELECT COUNT(*)   FROM students;           -- how many rows
SELECT AVG(age)   FROM students;           -- average age
SELECT MIN(age), MAX(age) FROM students;   -- youngest, oldest
SELECT SUM(age)   FROM students;           -- total
```

## GROUP BY

`GROUP BY` runs the aggregate *per group* instead of over the whole table:

```sql
SELECT country, COUNT(*) AS student_count, AVG(age) AS avg_age
FROM students
GROUP BY country;
```

Result — one row per country:

| country | student_count | avg_age |
|---------|---------------|---------|
| UK      | 2             | 38.5    |
| US      | 1             | 29.0    |

The rule: every column in the `SELECT` must either be inside an aggregate or listed in `GROUP BY`.

## Filtering groups with HAVING

`WHERE` filters rows *before* grouping; `HAVING` filters groups *after*:

```sql
SELECT country, COUNT(*) AS n
FROM students
GROUP BY country
HAVING COUNT(*) > 1;     -- only countries with more than one student
```

Use `WHERE` to cut individual rows and `HAVING` to cut whole groups.

---
**Go deeper (open references):** [PostgreSQL: Aggregate functions](https://www.postgresql.org/docs/current/functions-aggregate.html) (PostgreSQL docs).
$md$, 'seed'),

('c3000000-0000-0000-0000-000000000702', 'Joining Tables', 1, 'reading', 14, $md$
# Joining Tables

Real databases split data across tables to avoid repetition. **Joins** stitch them back together using a shared key.

Suppose `students` has a `country_id`, and a separate `countries` table holds the names:

```sql
SELECT students.name, countries.name AS country
FROM students
JOIN countries ON students.country_id = countries.id;
```

The `ON` clause says how rows match: a student's `country_id` equals a country's `id`.

## Kinds of join

- **INNER JOIN** (the default `JOIN`) — keeps only rows that have a match on both sides.
- **LEFT JOIN** — keeps every row from the left table; columns from the right are `NULL` when there's no match.

```sql
SELECT s.name, e.course_id
FROM students s
LEFT JOIN enrollments e ON e.student_id = s.id;
```

A `LEFT JOIN` here lists every student, including those with no enrollments (their `course_id` shows `NULL`). Use it when you want "all of X, plus matching Y if it exists."

## Why this matters

This normalized design — facts in one place, referenced by key — is the foundation of relational databases, and the microservices in this very project each follow it inside their own database.

---
**Go deeper (open references):** [PostgreSQL: Joins](https://www.postgresql.org/docs/current/tutorial-join.html) (PostgreSQL docs) and [Join (SQL)](https://en.wikipedia.org/wiki/Join_(SQL)) on Wikipedia (CC-BY-SA).
$md$, 'seed');
