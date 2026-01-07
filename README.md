# CHESS MASTER ♟️

## Description
Chess Master is a Java-based chess game developed using **Swing GUI**.  
The project supports both **Player vs Player** and **Player vs Computer** modes.  
For the computer opponent, a **Minimax algorithm–based AI** is implemented to make intelligent move decisions.

This project demonstrates strong **object-oriented design**, **game logic**, and **basic artificial intelligence concepts**.

---

## Features
- Interactive **Swing-based graphical chess board**
- Player vs Player mode
- Player vs Computer mode
- **Minimax algorithm** for AI decision-making
- Standard chess board initialization
- Rule-based move handling
- Clean and modular code structure

---

## Artificial Intelligence (Minimax)
The Player vs Computer mode uses the **Minimax algorithm**, which simulates future game states and evaluates possible moves.
The AI selects the optimal move by assuming both players play optimally, making it suitable for a turn-based, zero-sum game like chess.

---

## Project Structure
src/
└── com/
└── chess/
├── gui/
│ └── Table.java // Swing GUI and board rendering
└── engine/
└── Chessv2.java // Main class and game engine

- `com.chess.gui` – Swing GUI components and user interaction  
- `com.chess.engine` – Core game logic, AI (Minimax), and application entry point  

---

## Technologies Used
- Programming Language: **Java**
- GUI Framework: **Swing**
- Concepts: **OOP (Encapsulation, Inheritance, Polymorphism)**
- Algorithm: **Minimax (Game AI)**
- IDE: IntelliJ IDEA

---

## How to Run
1. Clone the repository
2. Open the project in IntelliJ IDEA
3. Navigate to `com.chess.engine`
4. Run `Chessv2.java` (contains the `main` method)

---

## Learning Outcomes
- Hands-on experience with Java Swing GUI
- Practical implementation of Minimax algorithm
- Understanding of chess game mechanics
- Improved object-oriented programming skills

---

## Author
Tejaswini Pamidi


