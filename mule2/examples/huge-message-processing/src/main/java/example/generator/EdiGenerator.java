/**
 * Copyright (C) 2008 Maurice Zeijen <maurice@zeijen.net>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package example.generator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public class EdiGenerator {

	private final Random random = new Random(System.currentTimeMillis());

	private StringBuilder line;

	private PrintWriter writer;

	private File tempFile;

	private long productCodeCounter = 1000000;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Welcome to the product EDI file generator.");
		System.out.println("This generator will create a file with random product data.");
		System.out.print("Please enter the number of product to generate (default:100) : ");

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		int numProducts = 100;

		try {
			String input = br.readLine().trim();
			if(input.length() > 0) {
				numProducts = Integer.parseInt(input);

				if(numProducts <= 0) {
					throw new NumberFormatException();
				}
			}
		} catch (IOException ioe) {
			System.out.println("IO error trying to read the number of products!");
			System.exit(1);
		} catch (NumberFormatException  e) {
			System.out.println("The value you entered isn't a positive number.");
			System.exit(1);
		}

		EdiGenerator generator = new EdiGenerator();

		try {
			generator.generate(numProducts);
		} catch (Exception e) {

			e.printStackTrace();
		}

	}

	public void generate(int numProducts) throws Exception {
		System.out.println("Generating products export file with "+ numProducts + " product" + ((numProducts > 1)? "s" : ""));
		try {
			try {
				createOutputWriter();

				int lastProgress = 0;
				for(int i = 0; i < numProducts; i++) {

					boolean completeSystem = random.nextBoolean();

					writeProduct(completeSystem);

					int progress = Math.round((float)(i+1.0) / numProducts * 100);

					if(progress % 10 == 0 && progress != lastProgress) {
						lastProgress = progress;
						System.out.print(progress + "% ");
					}
				}

				System.out.println();

			} finally {
				closeOutputWriter();
			}
		} catch (Exception e) {
			System.out.println("Something went wrong wile trying to generate the file.");
			System.out.println("Deleting faulty export file from temp folder.");

			deleteTempFile();

			throw e;
		}

		File destFile = new File(outputDirectory, "products.edi");
		moveTempFile(destFile);

		System.out.println("Finished generating file (size " + getFileSize(destFile) + ")");
	}


	/**
	 *
	 */
	private void writeProduct(boolean completeSystem) {
		newLine();

		writeField("PRD");
		writeField(++productCodeCounter);
		writeField(completeSystem ? randomEntry(completeSystemNames) : randomEntry(names));
		writeField(randomEntry(brands));
		writeField(completeSystem ? "Complete System" : randomEntry(categoryCodes));
		writeField(randomMoney());
		writeField("19");
		writeField(randomInt(1, 10) + " years");

		writeLine();

		if(completeSystem) {
			writeParts();
		}
		writeSpecs();

		flush();
	}


	/**
	 *
	 */
	private void writeParts() {

		int numParts = randomInt(1, 9);
		for(int i = 0; i < numParts; i++) {

			newLine();

			writeField("PRT");
			writeField(randomEntry(partCodes));
			writeField(randomInt(1, 10));
			writeField(Boolean.toString(random.nextBoolean()));

			writeLine();
		}
	}

	/**
	 *
	 */
	private void writeSpecs() {

		int numParts = randomInt(0, 5);
		for(int i = 0; i < numParts; i++) {
			newLine();

			writeField("SPC");

			Entry<String, String> entry = randomEntry(specs);

			writeField(entry.getKey());
			writeField(entry.getValue());

			writeLine();
		}
	}

	private int randomInt(int min, int max) {

		return random.nextInt(max - min + 1) + min;

	}

	private String randomMoney() {

		return moneyFormat.format((0.1 + random.nextInt(99999)) / 100);

	}

	private String randomEntry(String[] array) {

		return array[random.nextInt(array.length)];

	}

	private <K, V> Entry<K, V> randomEntry(Map<K, V> map) {

		int t = random.nextInt(map.keySet().size());
		int i = 0;
		for(Entry<K, V> entry : map.entrySet()) {
			if(i == t) {
				return entry;
			}
			i++;
		}
		return null;
	}


	private void writeField(long value) {
		writeField(Long.toString(value));
	}

	private void writeField(String value) {
		if(line.length() > 0) {
			line.append(SEP);
		}
		line.append(value);
	}

	private void newLine() {
		line = new StringBuilder();
	}

	private void writeLine() {
		writer.println(line);
	}

	private void flush() {
		writer.flush();
	}

	private void createOutputWriter() throws Exception {

		tempFile = new File("data", "products.edi.generating");
		if(tempFile.exists()) {
			tempFile.delete();
		}
		tempFile.createNewFile();

		writer = new PrintWriter(new FileOutputStream(tempFile), false);

	}

	private void closeOutputWriter(){

		if(writer != null) {
			writer.close();
		}

	}

	private void moveTempFile(File destFile){

		if(destFile.exists()) {
			destFile.delete();
		}

		tempFile.renameTo(destFile);

	}

	private void deleteTempFile(){

		tempFile.delete();

	}


	private String getFileSize(File file) {

		MathContext mc = new MathContext(3);

		long size = file.length();

		if(size < 1024) {

			return size + " bytes";

		} else if (size < 1024 * 1024) {

			return new BigDecimal(size).divide(new BigDecimal(1024), mc).toPlainString()  + " KB";

		} else if (size < 1024 * 1024 * 1024) {

			return new BigDecimal(size).divide(new BigDecimal(1024 * 1024), mc).toPlainString()  + " MB";

		} else {

			return new BigDecimal(size).divide(new BigDecimal(1024 * 1024 * 1024), mc).toPlainString()  + " GB";

		}

	}

	static {
		Locale.setDefault(Locale.US);
	}

	private static final String SEP = "|";

	private static final File outputDirectory = new File("data", "in");

	private static final DecimalFormat moneyFormat = new DecimalFormat("0.00");

	private static final String[] categoryCodes = new String[] {
		     //"Complete System", => This category is special and is coded into the logic to appear a lot more often
			"Laptops",
			"Graphic cards",
			"Input devices",
			"Harddisks",
			"Memory",
			"Processors",
			"Screens",
			"Webcams",
			"Motherboards"
	};

	private static final String[] brands = new String[] {
			"Creamit",
			"BVideo",
			"Blony",
			"Pear",
			"Freeflop",
			"XYZ-Blink",
			"Valtek",
			"DP",
			"Danon",
			"Brokia"
	};

	private static final String[] names = new String[] {
			"Mighty Mouse",
			"EES 5D",
			"B-FI XTreme Music PCI",
			"Via Verto S20",
			"TouchMe 2.5",
			"K-96 BD",
			"ICool X5584",
			"2GB (2x 1GB2) DDR2 PC2-8500",
			"4GB (2x 2GB2) DDR2 PC2-8500",
			"Flash Supreme 200GB",
			"Traveler 200GB",
			"BetterCam Ultra NightVision 54D",
			"BetterCam Super 52D",
			"Camelless 33S",
			"CD-5424 18x",
			"HDVD-32424 200x"
	};

	private static final String[] partCodes = new String[] {
		"100001",
		"100002",
		"100003",
		"100004",
		"100005",
		"100006",
		"100007",
		"100008",
		"100009",
	};

	private static final String[] completeSystemNames = new String[] {
		"Avia 622",
		"Avia X24",
		"Avia C44",
		"Supreme Gaming Machine 600",
		"Supreme Gaming Machine 500",
		"Supreme Gaming Machine 500 XD",
		"DownLiant L5800",
		"DownLiant S5300",
		"DownLiant DD5300",
	};

	private static final Map<String, String> specs = new HashMap<String, String>();

	static {

		specs.put("Card Interface", "PCI");
		specs.put("Memory Module", "1GB");
		specs.put("Memory Size", "4GB");
		specs.put("Max Resolution", "1920x1200");
		specs.put("Width", "518mm");
		specs.put("Height", "6000mm");
		specs.put("Reaction Time", "5ms");
		specs.put("Contrast", "300cd/m²");
		specs.put("Connection", "D-Sub");
		specs.put("LCD Panel", "TN");
		specs.put("Power", "2258W");
		specs.put("Color", "Pink, Green");
		specs.put("Length", "5M");
		specs.put("Weight", "6kg");
	}

}
