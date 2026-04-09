# Python Interpreter in Java

A lightweight Java-based interpreter that parses and executes a simple Python-like language. This project demonstrates how to build core interpreter components including lexical analysis, parsing, abstract syntax tree (AST) construction, and evaluation.

***🛠️ Entire workflow design of this project is done without AI by me(Advait), so there may be some issues. If found, please report! 🐛 Just a hobby project, built for fun! 🎉***

### General Workflow

![canvas](https://github.com/user-attachments/assets/058e4996-adf8-4e5c-8090-e9271916e406)

### Technical Calls
![canvas](https://github.com/user-attachments/assets/ad066263-f31e-49a7-aeb5-5c31a198bc59)

---

## 🚀 Features

- **Lexer**: Tokenizes input source code into meaningful symbols.
- **Parser**: Constructs an Abstract Syntax Tree (AST) from tokens.
- **AST**: Represents the syntactic structure of the code.
- **Interpreter**: Evaluates the AST within an environment that maintains variable bindings.
- **Environment**: Stores and manages variable scopes and values.
- **Call Stack**: Safe execution, recursion limiting, and error backtracing.
- **Built-in Functions**: Standard functions like `str()`, `len()`, `abs()`, etc.
- **Input Support**: Reads source code from `input.txt` for interpretation.

---

## 📁 File Structure

- `Lexer.java`: Lexical analyzer for token generation.
- `Parser.java`: Parses tokens into an AST.
- `AST.java`: Defines node types of the AST.
- `Interpreter.java`: Core evaluator of AST nodes.
- `Environment.java`: Manages variable bindings.
- `CallStack.java`: Tracks active stack frames and provides stack traces.
- `BuiltinsRegistry.java`: Registration of standard system functions.
- `Token.java`: Token definitions and types.
- `Main.java`: Entry point of the interpreter.
- `input.txt`: Input file containing the source code to interpret.

---

## 🧠 Syntax Guide

### 🧮 Variable Declaration

```plaintext
let <identifier> = <expression>
```

**Example:**
```plaintext
let x = 5
let name = "John"
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
let sum = 10 + 20
let product = x * 5
let result = (x + y) * 2
```

---

### 🖨️ Print Statement

```plaintext
print(<expression>)
```

**Example:**
```plaintext
print(x)
print("Hello, World!")
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
let result = (2 + 3) * 4  # result = 20
```

---

### 📝 Sample Program

```plaintext
let x = 10
let y = 20
let sum = x + y
print("Sum of x and y:")
print(sum)
```

**Output:**
```plaintext
Sum of x and y:
30
```
## ⚡ Performance Benchmark

We compared integer counting and recursive function invocations (`factorial(10)` run 100,000 times) against established engines.

| Engine / Implementation  | Simple Loop (V1) | Factorial Recursion (100k) |
|--------------------------|------------------|----------------------------|
| **Native Java loop**     | ~50–150 ms       | ~23 ms                     |
| **Python 3**             | ~200–600 ms      | ~141 ms                    |
| **Node.js (JS)**         | ~150–500 ms      | ~152 ms                    |
| **JavaInterpreter**      | ~371 ms ✅        | ~891 ms ✅                  |


---
## 🛠️ Getting Started

### Prerequisites

- Java Development Kit (JDK) 8 or higher

### Compile and Run

```bash
javac *.java
java Main
```

Make sure to edit `input.txt` with your program before running.

---

## 🏗️ Architecture

```mermaid
classDiagram
    class Main {
        +main(args)
    }
    class Lexer {
        -String input
        +tokenize() List~Token~
    }
    class Parser {
        -List~Token~ tokens
        +parse() ASTNode
    }
    class Interpreter {
        +execute(ASTNode)
    }
    class Environment {
        -Environment parent
        -CallStack callStack
        +define(name, value)
        +get(name) Object
        +defineFunction(name, func)
        +defineBuiltin(name, func)
    }
    class CallStack {
        -Stack~StackFrame~ frames
        +push(name, args)
        +pop()
        +formatTrace() String
    }
    class BuiltinsRegistry {
        +register(Environment)
    }
    class ASTNode {
        <<abstract>>
        +evaluate(Environment) Object
    }
    
    Main "1" --> "1" Lexer : creates
    Main "1" --> "1" Parser : creates
    Main "1" --> "1" Interpreter : creates
    Parser "1" --> "*" ASTNode : generates
    Interpreter "1" --> "1" Environment : creates
    Interpreter "1" --> "1" BuiltinsRegistry : configures
    Environment "1" *-- "1" CallStack : owns
    ASTNode "*" ..> "1" Environment : accesses
```

---

## 🗺️ Roadmap

- [x] **Conditionals**: `if`, `else`, and comparison operators
- [x] **Loops**: `while`, `for`, and control flow
- [x] **Functions**: Definition and calls with parameters
- [x] **Call Stack**: Stack traces and recursion limits
- [x] **Boolean Logic**: `true`, `false`, `&&`, `||`, `!`
- [ ] **Comments**: Ignoring lines with `//`
- [ ] **Arrays and Objects**: Composite data structures


---

## 🤝 Contributing

Pull requests and suggestions are welcome. Fork the repo and open an issue or PR.

---

## 📜 License

This project is licensed under the [MIT License](LICENSE).

---

## 👤 Author
**[Advait Sankhe](https://github.com/AdvaitSan)**

