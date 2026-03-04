# Image Classification via Genetic Programming (TinyGP)

An evolutionary computation project that evolves mathematical functions to classify geometric shapes (circles and squares) from extracted visual features. The core logic is based on a custom Java implementation of the **TinyGP** algorithm.

##  Project Concept
Unlike traditional Deep Learning, this project uses **Genetic Programming (GP)** to discover a symbolic expression that best separates two classes of synthetic images. The agent evolves through generations, optimizing its "DNA" (mathematical operators and terminals) to minimize classification error.

##  The Pipeline
1. **Dataset Generation:** Created 1,000 synthetic images of circles (varying radii) and squares (random rotations/scales), including edge cases with cropped figures.
2. **Feature Extraction:** - Grayscale conversion & Thresholding.
   - Contour detection via `findContours`.
   - Quantitative feature normalization ($X_1, X_2, X_3, X_4$).
3. **Evolutionary Cycle (TinyGP):**
   - **Representation:** Syntax trees composed of arithmetic functions (ADD, SUB, MUL, DIV) and terminals.
   - **Selection:** Tournament selection (TSIZE) for parent breeding.
   - **Genetic Operators:** Sub-tree Crossover and Point Mutation.
   - **Fitness Function:** Minimized absolute error transformed via a Sigmoid function for probability mapping.

##  Key Results
- **Accuracy:** Achieved **>90% accuracy** on unseen test datasets.
- **Model Interpretability:** The final output is a symbolic mathematical formula (`best_program.txt`), allowing for full transparency of the decision-making process.
- **Efficiency:** Used a steady-state evolutionary approach to update the population (5,000 individuals) over 800 generations.

##  Tech Stack
- **Language:** Java
- **Domain:** Genetic Programming, Symbolic Regression, Computer Vision.
- **Tools:** Custom TinyGP adaptation, Image Processing primitives.
