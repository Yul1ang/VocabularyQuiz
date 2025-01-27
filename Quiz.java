package quiz;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Collections;

public class Quiz {

	private static Map<String, Mean> vocabularies = new HashMap<>();
	private static Map<Integer, ArrayList<String>> weightListVocabulary = new HashMap<>();
	private static Map<String, Integer> weightVocabulary = new HashMap<>();
	private static final String VOCABULARY_FILE = "vocabularyC1.txt"; // TODO: Independent for each one
	private static final String FORGETTING_FILE = "weightC1.txt"; // TODO: Independent for each one

	public static void main(String[] args) throws IOException {
		loadVocabulary();
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				System.out.println("Saving result...");
				finishQuiz();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}));
		startQuiz();
	}

	private static void loadVocabulary() throws IOException {
		BufferedReader ptrv = new BufferedReader(new FileReader(VOCABULARY_FILE));
		BufferedReader ptrf = new BufferedReader(new FileReader(FORGETTING_FILE));
		BufferedWriter ptwf = new BufferedWriter(new FileWriter(FORGETTING_FILE));

		String line = null;
		while ((line = ptrf.readLine()) != null) {
			String lineS[] = line.split("-");
			if (lineS.length == 2) {
				weightVocabulary.put(lineS[0].trim(), Integer.parseInt(lineS[1].trim()));
				ArrayList<String> aux = weightListVocabulary.getOrDefault((Integer.parseInt(lineS[1].trim())),
						new ArrayList<>());
				aux.add(lineS[0].trim());
				weightListVocabulary.put((Integer.parseInt(lineS[1].trim())), aux);
			}
		}

		line = null;
		while ((line = ptrv.readLine()) != null) {
			String lineS[] = line.split("-");
			if (lineS.length == 3) {
				vocabularies.put(lineS[0].trim(), new Mean(lineS[1].trim(), lineS[2].trim()));
				if (!weightVocabulary.containsKey(lineS[0].trim())) {
					weightVocabulary.put(lineS[0].trim(), 0);
					ArrayList<String> aux = weightListVocabulary.getOrDefault(0, new ArrayList<>());
					aux.add(lineS[0].trim());
					weightListVocabulary.put(0, aux);
				}
			}
		}

		ptrv.close();
		ptrf.close();
		ptwf.close();
	}

	private static void startQuiz() throws IOException {
		BufferedReader ptrInput = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("--------------------------- C1 Vocabulary Quiz ------------------------------------");
		Random random = new Random();
		Boolean exec = true;
		while (exec) {
			Integer randomCase = random.nextInt(2);
			switch (randomCase) {
			case 0:
				multipleChoise(getMinKey());
				break;
			case 1:
				simpleChoise(getMinKey());
				break;
			}
			String linea;
			if ((linea = ptrInput.readLine()) != null) {
				if (linea.trim().equals("exit")) {
					exec = false;
				}
			}
		}
	}

	private static void simpleChoise(String vocabulary) throws IOException {
		BufferedReader ptrInput = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("What is the meaning of [" + vocabulary + "]?");
		ptrInput.readLine();
		System.out.println("The correct answer is:\n" + vocabularies.get(vocabulary).getMeaning() + " (eg. "
				+ vocabularies.get(vocabulary).getExample() + ")\n" + "Did you get it right? (yes or no)");
		String linea;
		while ((linea = ptrInput.readLine()) != null) {
			if (linea.trim().toLowerCase().equals("yes")) {
				changeWeight(vocabulary, true);
				break;
			} else if (linea.trim().toLowerCase().equals("no")) {
				changeWeight(vocabulary, false);
				break;
			} else {
				System.out.println("Invalid option");
			}
		}
	}

	private static void changeWeight(String vocabulary, boolean weightInc) {
//		if (weightListVocabulary.get(weightVocabulary.get(vocabulary)) != null) {
		ArrayList<String> aux1 = weightListVocabulary.get(weightVocabulary.get(vocabulary));
		aux1.remove(vocabulary);
		weightListVocabulary.put(weightVocabulary.get(vocabulary), aux1);
//		} else {
//			weightListVocabulary.remove(weightVocabulary.get(vocabulary));
//		}

		if (weightInc) {
			weightVocabulary.put(vocabulary, weightVocabulary.get(vocabulary) + 1);
			ArrayList<String> aux2 = weightListVocabulary.getOrDefault(weightVocabulary.get(vocabulary),
					new ArrayList<>());
			aux2.add(vocabulary);
			weightListVocabulary.put(weightVocabulary.get(vocabulary), aux2);
		} else {
			weightVocabulary.put(vocabulary, 0);
			ArrayList<String> aux2 = weightListVocabulary.getOrDefault(0, new ArrayList<>());
			aux2.add(vocabulary);
			weightListVocabulary.put(0, aux2);
		}
	}

	private static void finishQuiz() throws IOException {
		BufferedWriter ptwf = new BufferedWriter(new FileWriter(FORGETTING_FILE));
		for (Map.Entry<String, Integer> e : weightVocabulary.entrySet()) {
			ptwf.write(e.getKey() + " - " + e.getValue() + "\n");
		}
		ptwf.close();
	}

	private static void multipleChoise(String vocabulary) throws IOException {
		BufferedReader ptrInput = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("What is the meaning of [" + vocabulary + "]?");
		ArrayList<String> randomV = getOt3RandomKeys(vocabulary);
		randomV.add(vocabulary);
		Collections.shuffle(randomV);
		for (int i = 0; i < randomV.size(); i++) {
			System.out.println(i + ". " + vocabularies.get(randomV.get(i)).getMeaning() + " (eg. "
					+ vocabularies.get(randomV.get(i)).getExample() + ")");
		}
		String option = null;
		while ((option = ptrInput.readLine()) != null) {
			try {
				int optionN = Integer.parseInt(option.trim());
				if (optionN < randomV.size()) {
					checkOption(optionN, randomV, vocabulary);
					break;
				} else {
					System.out.println("Invalid option");
				}
			} catch (NumberFormatException e) {
				System.out.println("Invalid option");
			}
		}
	}

	private static void checkOption(int optionN, ArrayList<String> randomV, String vocabulary) {
		if (vocabularies.get(randomV.get(optionN)).equals(vocabularies.get(vocabulary))) {
			System.out.println("CORRECT");
			changeWeight(vocabulary, true);
		} else {
			System.out.println("INCORRECT");
			System.out.println("The correct answer is: " + vocabularies.get(vocabulary).getMeaning() + " (eg. "
					+ vocabularies.get(vocabulary).getExample() + ")");
			changeWeight(vocabulary, false);
		}
	}

	private static String getMinKey() {
		String res = null;
		for (int i = 0; i < weightListVocabulary.size(); i++) {
			if (weightListVocabulary.containsKey(i) && weightListVocabulary.get(i) != null
					&& weightListVocabulary.get(i).size() != 0) {
				res = (String) getRandomKey(weightListVocabulary.get(i));
				break;
			}
		}
		return res;
	}

	private static Object getRandomKey(ArrayList<?> list) {
		Random random = new Random();
		int randomIndex = random.nextInt(list.size());
		return list.get(randomIndex);
	}

	private static ArrayList<String> getOt3RandomKeys(String vocabulary) {
		ArrayList<String> res = new ArrayList<>();
		ArrayList<String> randomKey = new ArrayList<>(vocabularies.keySet());
		Random random = new Random();
		for (int i = 0; i < 3; i++) {
			String aux = randomKey.get(random.nextInt(vocabularies.size()));
			while (aux.equals(vocabulary)) {
				aux = randomKey.get(random.nextInt(vocabularies.size()));
			}
			res.add(aux);
		}
		return res;
	}

	static class Mean {
		private String meaning;
		private String example;

		public Mean(String meaning, String example) {
			this.meaning = meaning;
			this.example = example;
		}

		public String getMeaning() {
			return meaning;
		}

		public String getExample() {
			return example;
		}
	}
}
