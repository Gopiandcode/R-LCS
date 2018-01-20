package com.gopiandcode.lcs.dataset.generators;

import com.gopiandcode.lcs.dataset.BinaryClassifierTestData;
import com.gopiandcode.lcs.dataset.GeneratedMultiplexerBinaryClassifierDataset;
import com.gopiandcode.lcs.problem.BinaryAlphabet;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class LocalMultiplexerDatasetGenerator {
    private final int problemCount;
    private final int controlBits;
    private final String outputname;

    public LocalMultiplexerDatasetGenerator(int problemCount, int controlBits, String outputname) {
        this.problemCount = problemCount;
        this.controlBits = controlBits;
        this.outputname = outputname;
    }

    public void generateDataset() throws FileNotFoundException {
        StringBuilder sb = new StringBuilder();
        sb.append("controlBits: " + controlBits + "\n");
        sb.append("totalSize: " + ((int) (controlBits + Math.pow(2, controlBits)))+ "\n");
        sb.append("count: " + problemCount + "\n");
        GeneratedMultiplexerBinaryClassifierDataset generator = new GeneratedMultiplexerBinaryClassifierDataset(controlBits);
       for(int i = 0; i < problemCount; i++) {
           BinaryClassifierTestData binaryClassifierTestData = generator.nextDataPoint();
           BinaryAlphabet[] values = binaryClassifierTestData.getSituation().getValues();
           BinaryAlphabet classification = binaryClassifierTestData.getAction().getClassification();
           for (BinaryAlphabet value : values) {
               switch (value) {
                   case ONE:
                       sb.append("1");
                       break;
                   case ZERO:
                       sb.append("0");
                       break;
               }
           }
           sb.append(" ");
           switch(classification) {
               case ONE:
                   sb.append("1");
                   break;
               case ZERO:
                   sb.append("0");
                   break;
           }
           sb.append("\n");
       }

       PrintWriter printWriter = new PrintWriter(outputname);
       printWriter.write(sb.toString());
       printWriter.flush();
       printWriter.close();
    }

    public static void main(String[] args) throws FileNotFoundException {
        LocalMultiplexerDatasetGenerator gen = new LocalMultiplexerDatasetGenerator(10000, 2, "testData6.txt");
        gen.generateDataset();
    }
}
