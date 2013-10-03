package org.gd.spark.opendl.example.spark;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.gd.spark.opendl.downpourSGD.SGDTrainConfig;
import org.gd.spark.opendl.downpourSGD.SampleVector;
import org.gd.spark.opendl.downpourSGD.lr.LR;
import org.gd.spark.opendl.downpourSGD.lr.LRTrain;
import org.gd.spark.opendl.example.ClassVerify;
import org.gd.spark.opendl.example.DataInput;

public class LRTest {
	private static final Logger logger = Logger.getLogger(LRTest.class);
	
	public static void main(String[] args) {
		try {
			int x_feature = 784;
			int y_feature = 10;
			List<SampleVector> samples = DataInput.readMnist("mnist_784_1000.txt", x_feature, y_feature);
			
			List<SampleVector> trainList = new ArrayList<SampleVector>();
			List<SampleVector> testList = new ArrayList<SampleVector>();
			DataInput.splitList(samples, trainList, testList, 0.8);
			
			JavaSparkContext context = SparkContextBuild.getContext(args);
			JavaRDD<SampleVector> rdds = context.parallelize(trainList);
			rdds.count();
			logger.info("RDD ok.");
			
			LR lr = new LR(x_feature, y_feature);
            SGDTrainConfig config = new SGDTrainConfig();
            config.setUseCG(true);
            config.setCgEpochStep(100);
            config.setCgTolerance(0);
            config.setCgMaxIterations(30);
            config.setMaxEpochs(100);
            config.setNbrModelReplica(4);
            config.setMinLoss(0.01);
            config.setUseRegularization(true);
            
            logger.info("Start to train lr.");
            LRTrain.train(lr, rdds, config);
            
            int trueCount = 0;
            int falseCount = 0;
            double[] predict_y = new double[y_feature];
            for(SampleVector test : testList) {
            	lr.predict(test.getX(), predict_y);
            	if(ClassVerify.classTrue(test.getY(), predict_y)) {
            		trueCount++;
            	}
            	else {
            		falseCount++;
            	}
            }
            logger.info("trueCount-" + trueCount + " falseCount-" + falseCount);
		} catch(Throwable e) {
			logger.error("", e);
		}
	}

}
