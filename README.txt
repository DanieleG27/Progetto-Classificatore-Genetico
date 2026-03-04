===============================================================================================================
Struttura del progetto
===============================================================================================================

- best_program.txt
	Contiene la funzione evoluta da parte del TinyGP, serve da input per classificator.py


- classification_results.csv e classification_results_with_truth.csv
	Sono due DataFrame che contengono i risultati della classificazione
	(con anche la colonna della label reale).


- classificator.py
	_ Script Python che si occupa della lettura della cartella di testing.
	_ Effettua la classificazione di ogni immagine presente nella cartella.
	_ Calcola l'accuracy.


- createDataset.ipynb
	Jupyter Notebook per creare il dataset sintetico con cerchi e quadrati.


- createProblemDat.py
	Script Python che si occupa di estrarre le feature e salvarle nel file probelm.dat 
	che sarà l'input di TinyGP.


- TinyGP.java
	Progrmma Java che realizza il core del progetto.
	Prende in input il file contenente le feature delle immagini.
	Evolve la formula partendo da una popolazione e applicando selezione, crossover, mutazione.
	Dovrà essere lanciato con "java .\Tiny_GP.java problem.dat "
	

===============================================================================================================
Contenuto delle cartelle
===============================================================================================================

- Dataset: immagine sintetiche usate per il training.
- Documenti: paper sulla programmazione genetica con riferimenti al TinyGP e presentazione progetto
- Testing: Parte di dataset usata solo per il testing