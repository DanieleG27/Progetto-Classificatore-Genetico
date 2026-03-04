package daniele.unibo;

import java.util.*;
import java.io.*;
import java.text.DecimalFormat;



public class Tiny_GP {
	
	double [] fitness;	//fitness di ogni individuo
	char [][] pop;		//array di individui
	static Random rd = new Random();
	
	static final int
		ADD= 110,
		SUB= 111,
		MUL= 112,
		DIV= 113,
		FSET_START= ADD,
		FSET_END= DIV;
	
	static double [] x= new double[FSET_START];	//valori correnti delle variabili durante l’esecuzione
	static double minrandom, maxrandom;
	static char[] program;
	static int PC;
	static int varnumber, fitnesscases, randomnumber;
	static double fbestpop=0.0, favgpop=0.0;
	static long seed;
	static double avg_len;
	
	static final int
		MAX_LEN= 10000,
		POPSIZE= 5000,
		DEPTH=4,
		GENERATIONS=800,
		TSIZE=2;
	
	public static final double
		PMUT_PER_NODE = 0.05,
		CROSSOVER_PROB=0.9;
	
	static double [][] targets;	//ogni rigaa
	
	double run() {
		char primitive= program[PC++];
		if(primitive < FSET_START) {
			return(x[primitive]);			
		}
		switch(primitive) {
			case ADD: return (run()+run());
			case SUB: return (run()-run());
			case MUL: return (run()*run());
			case DIV: {
				double num = run(), den = run();
				if(Math.abs(den)<= 0.001)
					return num;
				else return num/den;
			}
		}
		return (0.0);
	}
	
	//Conta ricorsivamente quanti nodi appartengono al sottoalbero (lunghezza del programma)
	int traverse(char[] buffer, int buffercount) {
		if(buffer[buffercount]< FSET_START)
			return (++buffercount);
		
		switch(buffer[buffercount]) {
			case ADD:
			case SUB: 
			case MUL: 
			case DIV: 
			return (traverse(buffer, traverse(buffer, ++buffercount)));
					
		}
		return 0;
	}
	
	//legge le variabili e i valori target scritti su file 
	void setup_fitness(String fname) {
		try {
			int i,j;
			String line;
			
			BufferedReader in = new BufferedReader(new FileReader(fname));
			line= in.readLine();
		
			StringTokenizer tokens = new StringTokenizer(line);
			varnumber = Integer.parseInt(tokens.nextToken().trim());
			randomnumber = Integer.parseInt(tokens.nextToken().trim());
			minrandom= Double.parseDouble(tokens.nextToken().trim());
			maxrandom= Double.parseDouble(tokens.nextToken().trim());
			fitnesscases = Integer.parseInt(tokens.nextToken().trim());
			
			targets = new double[fitnesscases][varnumber+1];
			
			if(varnumber+randomnumber >= FSET_START)
				System.out.println("Too many variables and costants");
			
			for(i=0; i<fitnesscases; i++) {
				line= in.readLine();
				tokens= new StringTokenizer(line);
				
				//Le feature sono nei primi varnumber elementi
				//Il target (cioè l’output desiderato) è nell’ultimo (targets[i][varnumber])
				for(j=0; j<=varnumber; j++) {
					targets[i][j] = Double.parseDouble(tokens.nextToken().trim());					
				}
			}
			in.close();			
		}catch(FileNotFoundException e) {
			System.out.println("File not found");
			System.exit(0);
		}catch(Exception e) {
			System.out.println("Bad format");
			System.exit(0);
		}
	}
	
	
	
	double fitness_function(char [] Prog) {
		int  len= traverse(Prog, 0);
		double result, fit=0.0;
		
		for(int i=0; i<fitnesscases; i++) {
			for(int j=0; j<varnumber; j++){
				x[j]= targets[i][j];
			}
			program=Prog;
			PC=0;
			result = run();
			result = 1.0 / (1.0 + Math.exp(-result)); // sigmoide
			fit+=Math.abs(result-targets[i][varnumber]);
		}
		/*
		// --- penalità sulla lunghezza ---
		double penalty_weight = 0.0025; // puoi regolarla
		double length_penalty = penalty_weight * len;

		// ritorna fitness negativa (più è grande fit, peggio è)
		return -(fit + length_penalty);
		 */
		

		return(-fit);
	}
	
	
	int grow(char[] buffer, int pos, int max, int depth) {
		char prim = (char) rd.nextInt(2) ;
		
		if(pos>=max) return -1;
		if(pos==0) prim= 1;
		// --- caso TERMINALE ---
		if(prim==0 || depth==0) {
			prim = (char) rd.nextInt(varnumber+randomnumber);
			buffer[pos]=prim;
			return pos+1;
		}
		// --- caso FUNZIONE ---
		else {
			prim = (char) (rd.nextInt(FSET_END-FSET_START+1) + FSET_START); //scelto un simbolo dal set delle funzioni (ADD, SUB, MUL, DIV)
			switch(prim) {
			case ADD:
			case SUB:
			case MUL:
			case DIV:
				buffer[pos]=prim;
				return( grow(buffer, grow(buffer, pos+1, max, depth-1), max, depth-1) );
			}
		}
		return 0;
	}
	
	
	int print_indiv(char[] buffer, int buffercounter) {
		int a1=0, a2;
		
		if(buffer[buffercounter]< FSET_START) {
			if(buffer[buffercounter]<  varnumber) 
				System.out.print("X"+(buffer[buffercounter]+1));
			else
				System.out.printf("%.2f", x[buffer[buffercounter]]);
			return ++buffercounter;
		}
		
		switch (buffer[buffercounter]) {
			case ADD: 
				System.out.print("(");
				a1=print_indiv(buffer, ++buffercounter);
				System.out.print(" + ");
				break;
			case SUB: 
				System.out.print("(");
				a1=print_indiv(buffer, ++buffercounter);
				System.out.print(" - ");
				break;
			case MUL: 
				System.out.print("(");
				a1=print_indiv(buffer, ++buffercounter);
				System.out.print(" * ");
				break;
			case DIV: 
				System.out.print("(");
				a1=print_indiv(buffer, ++buffercounter);
				System.out.print(" / ");
				break;
			}
		
		a2=print_indiv(buffer, a1);
		System.out.print(")");
		return a2;
	}
	
	int print_indiv(char[] buffer, int buffercounter, PrintWriter out) {
		int a1 = 0, a2;

		if (buffer[buffercounter] < FSET_START) {
			if (buffer[buffercounter] < varnumber)
				out.print("X" + (buffer[buffercounter] + 1));
			else
				out.print(String.format("%.2f", x[buffer[buffercounter]]));
			return ++buffercounter;
		}

		switch (buffer[buffercounter]) {
			case ADD:
				out.print("(");
				a1 = print_indiv(buffer, ++buffercounter, out);
				out.print(" + ");
				break;
			case SUB:
				out.print("(");
				a1 = print_indiv(buffer, ++buffercounter, out);
				out.print(" - ");
				break;
			case MUL:
				out.print("(");
				a1 = print_indiv(buffer, ++buffercounter, out);
				out.print(" * ");
				break;
			case DIV:
				out.print("(");
				a1 = print_indiv(buffer, ++buffercounter, out);
				out.print(" / ");
				break;
		}

		a2 = print_indiv(buffer, a1, out);
		out.print(")");
		return a2;
	}
	


	static char[] buffer= new char[MAX_LEN];
	
	char[] create_random_indiv(int depth) {
		char[] ind;
		int len;
		
		len= grow(buffer, 0, MAX_LEN, depth);
		while(len<0)
			len=grow(buffer, 0, MAX_LEN, depth);
		
		ind= new char[len];
		System.arraycopy(buffer, 0, ind, 0, len);
		return ind;	
	}
	
	char[][] create_random_pop(int n, int depth, double[] fitness){
		char[][] pop = new char[n][];
		
		for(int i=0; i<n; i++) {
			pop[i]= create_random_indiv(depth);
			fitness[i]=fitness_function(pop[i]);
		}
		return pop;	
	}
	
	void stats(double[] fitness, char[][] pop, int gen) {
		int i, best=rd.nextInt(POPSIZE);
		int node_count=0;
		fbestpop= fitness[best];
		favgpop=0.0;
		
		for(i=0; i<POPSIZE;i++) {
			node_count+=traverse(pop[i],0);
			favgpop+=fitness[i];
			if(fitness[i]>fbestpop) {
				best=i;
				fbestpop=fitness[i];
			}		
		}
		avg_len=(double) node_count/POPSIZE;
		favgpop/= POPSIZE;
		System.out.println("Generation= "+gen+
						   "\nAvgFitness= "+(-favgpop)+
						   "\nBestFitness= "+(-fbestpop)+
						   "\nAvgSize= "+avg_len+
						   "\nBestIndividual= ");
		print_indiv(pop[best], 0);
		System.out.println("\n");
		System.out.flush();
	}
	
	
	int tournement(double[] fitness, int tsize) {
		int best=rd.nextInt(POPSIZE),i, competitor;
		double fbest=-1.0e34;
		
		for(i=0; i<tsize; i++) {
			competitor=rd.nextInt(POPSIZE);
			if(fitness[competitor]>fbest) {
				fbest=fitness[competitor];
				best=competitor;
			}
		}
		return best;
	}
	
	
	int negative_tournement(double[] fitness, int tsize) {
		int worst=rd.nextInt(POPSIZE),i, competitor;
		double fworst=1.0e34;
		
		for(i=0; i<tsize; i++) {
			competitor=rd.nextInt(POPSIZE);
			if(fitness[competitor]<fworst) {
				fworst=fitness[competitor];
				worst=competitor;
			}
		}
		return worst;
	}
	
	
	char[] crossover(char[] parent1, char[] parent2) {
		int xo1start, xo1end, xo2start, xo2end;
		char[] offspring;
		
		int len1=traverse(parent1,0);
		int len2=traverse(parent2,0);
		int lenoff;
		
		xo1start=rd.nextInt(len1);
		xo1end=traverse(parent1, xo1start);
		
		xo2start=rd.nextInt(len2);
		xo2end=traverse(parent2, xo2start);
		
		lenoff= xo1start + (xo2end-xo2start) + (len1-xo1end);
		
		offspring= new char[lenoff];
		
		System.arraycopy(parent1, 0, offspring, 0, xo1start);
		System.arraycopy(parent2, xo2start, offspring, xo1start, (xo2end-xo2start));
		System.arraycopy(parent1, xo1end, offspring, xo1start + (xo2end-xo2start), (len1-xo1end));
		
		return offspring;
	}
	
	
	
	
	char[] mutation(char[] parent, double pmut) {
		int len=traverse(parent, 0), i;
		int mutsite;
		char[] parentcopy= new char[len];
		
		System.arraycopy(parent, 0, parentcopy, 0, len);
		for(i=0; i<len; i++) {
			if(rd.nextDouble()<pmut) {
				mutsite=i;
				if(parentcopy[mutsite] < FSET_START)
					parentcopy[mutsite] = (char) rd.nextInt(varnumber);
				else
					switch(parentcopy[mutsite]) {
						case ADD:
						case SUB:
						case MUL:
						case DIV:
							parentcopy[mutsite]= (char) (rd.nextInt(FSET_END-FSET_START+1) + FSET_START);
					}
			}
		}
		return parentcopy;
	}
	
	
	void print_parms() {
		System.out.print("-- TINY GP (Java version) --\n");
		System.out.print("SEED= "+seed+"\nMAX LEN= "+MAX_LEN+
		"\nPOPSIZE= "+POPSIZE+
		" \nDEPTH= "+DEPTH+
		" \nCROSSOVER PROB= "+CROSSOVER_PROB+
		" \nPMUT PER NODE= "+PMUT_PER_NODE+
		" \nMIN RANDOM= "+minrandom+
		" \nMAX RANDOM= "+maxrandom+
		" \nGENERATIONS= "+GENERATIONS+
		" \nTSIZE= "+TSIZE+
		" \n-------------------------------------------\n" ) ;
	 }
		
	
	public Tiny_GP(String fname, long s) {
		fitness = new double[POPSIZE];
		seed=s;
		if(seed>=0)
			rd.setSeed(seed);
		setup_fitness(fname);
		pop = create_random_pop(POPSIZE, DEPTH, fitness);
		for(int i=0; i<FSET_START; i++)
			x[i] = (maxrandom-minrandom) * rd.nextDouble() + minrandom;
	}
	
	
	
	void evolve() {
		int gen=0, indivs, offspring, parent1, parent2, parent;
		double newfit;
		char[] newind;
		
		print_parms();
		stats(fitness, pop, 0);
		
		for(gen=1; gen<GENERATIONS; gen++) {
			
			if(fbestpop > -1e-5) {
				System.out.println("PROBLEM_SOLVED\n");

				// Trova il miglior individuo corrente
				int best = 0;
				for (int i = 1; i < POPSIZE; i++) {
					if (fitness[i] > fitness[best]) {
						best = i;
					}
				}

				// Salva il miglior individuo trovato
				try (PrintWriter out = new PrintWriter("best_program.txt")) {
					System.out.println("Stampo su file il best program\n");
					print_indiv(pop[best], 0, out);
					out.println();
					out.println("BestFitness = " + (-fbestpop));
					out.println("Generation = " + gen);
				} catch (Exception e) {
					e.printStackTrace();
				}

				System.exit(0);
			}

			for(indivs=0; indivs<POPSIZE; indivs++) {
				if(rd.nextDouble() > CROSSOVER_PROB) {
					parent1= tournement(fitness, TSIZE);
					parent2= tournement(fitness, TSIZE);
					
					newind= crossover(pop[parent1], pop[parent2]);					
				}else {
					parent = tournement(fitness, TSIZE);
					newind= mutation(pop[parent], PMUT_PER_NODE);
				}
				newfit= fitness_function(newind);
				offspring= negative_tournement(fitness, TSIZE);
				pop[offspring]= newind;
				fitness[offspring]= newfit;
			}
			stats(fitness, pop, gen);
		}
		System.out.println("PROBLEM NOT SOLVED\n");
		System.exit(1);
	}
	
	public static void main(String[] args) {
		String fname= "problem.dat";
		long s=-1;
		
		if(args.length==2) {
			s= Integer.valueOf(args[0]).intValue();
			fname= args[1];
		}
		if(args.length==1) {
			fname= args[0];
		}
		
		Tiny_GP gp = new Tiny_GP(fname, s);
		gp.evolve();
	}
		

}
