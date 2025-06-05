<<<<<<< HEAD
"# JavaInterpreter" 
=======
# Java-Interpreter

A lightweight Java-based interpreter that parses and executes a simple programming language. This project showcases the implementation of core interpreter components such as lexical analysis, parsing, abstract syntax tree (AST) construction, and evaluation.

![canvas](https://github.com/user-attachments/assets/058e4996-adf8-4e5c-8090-e9271916e406)

---

## 🚀 Features

- **Lexer**: Tokenizes input source code into meaningful symbols.
- **Parser**: Constructs an Abstract Syntax Tree (AST) from tokens.
- **AST**: Represents the syntactic structure of the code.
- **Interpreter**: Evaluates the AST within an environment that maintains variable bindings.
- **Environment**: Stores and manages variable scopes and values.
- **Input Support**: Reads source code from `input.txt` for interpretation.

---

## 📁 File Structure

- `Lexer.java`: Lexical analyzer for token generation.
- `Parser.java`: Parses tokens into an AST.
- `AST.java`: Defines node types of the AST.
- `Interpreter.java`: Core evaluator of AST nodes.
- `Environment.java`: Manages variable bindings.
- `Token.java`: Token definitions and types.
- `Main.java`: Entry point of the interpreter.
- `input.txt`: Input file containing the source code to interpret.

---

## 🧠 Syntax Guide

### 🧮 Variable Declaration

```plaintext
let <identifier> = <expression>;
```

**Example:**
```plaintext
let x = 5;
let name = "John";
```

---

### ➕ Arithmetic Operations

**Supported Operators:**
- `+` (Addition)
- `-` (Subtraction)
- `*` (Multiplication)
- `/` (Division)

**Example:**
```plaintext
let sum = 10 + 20;
let product = x * 5;
let result = (x + y) * 2;
```

---

### 🖨️ Print Statement

```plaintext
print(<expression>);
```

**Example:**
```plaintext
print(x);
print("Hello, World!");
```

---

### 🧠 Expressions

Can include:
- Integers: `10`, `-5`
- Strings: `"hello"`
- Identifiers: `x`, `y`
- Compound expressions: `x + y * 2`

---

### 📂 Order of Operations

Standard precedence:
1. Parentheses `()`
2. Multiplication / Division `* /`
3. Addition / Subtraction `+ -`

**Example:**
```plaintext
let result = (2 + 3) * 4;  // result = 20
```

---

### 📝 Sample Program

```plaintext
let x = 10;
let y = 20;
let sum = x + y;
print("Sum of x and y:");
print(sum);
```

**Output:**
```plaintext
Sum of x and y:
30
```

---

## 🛠️ Getting Started

### Prerequisites

- Java Development Kit (JDK) 8 or higher.

### Compile and Run

```bash
javac *.java
java Main
```

Make sure to edit `input.txt` with your program before running.

---

## 🗺️ Future Plans

Support for the following is planned in future versions:

- [x] **Conditionals**: `if`, `else`, and comparison operators.
- [x] **Loops**: `while`, `for`, and control flow.
- [ ] **Functions**: Definition and calls with parameters.
- [ ] **Boolean Logic**: `true`, `false`, `&&`, `||`, `!`.
- [ ] **Comments**: Ignoring lines with `//`.
- [ ] **Arrays and Objects**: Composite data structures..

---

## 🤝 Contributing

Pull requests and suggestions are welcome. Fork the repo and open an issue or PR.

---

## 📜 License

This project is licensed under the [MIT License](LICENSE).

---

## 👤 Author

**[Advait Sankhe](https://github.com/AdvaitSan)**
>>>>>>> 8ffaf994e44e068b637290e7d15ce59cb958c533
