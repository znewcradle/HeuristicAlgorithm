package tabooSearch;

import java.util.Random;
import pso.TestFunction;

/**
 * 利用正弦混沌和分阶段计算策略来改善连续禁忌搜索的全局搜索性能
 * @author Yunjia Xu
 *
 */
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import pso.TestFunction;

/**
 * 利用正弦混沌和分阶段计算策略来改善连续禁忌搜索的全局搜索性能
 * @author Yunjia Xu
 *
 */
public class LastImprovedCTSWithStages {
	//选择函数的类型
	/*
	 * 1--30  	2--120 	3--200 	4--2 5--6  6 --60 7--2
	 */
	private int fType = 6;
	//目标解的维度
	private static  int dimensions = 2;
	
	//初始的参数设置
	private int neighbors = 32;				//领域个数
	private int maxcycle  = 80;				//最大迭代次数,原来是80
	private double radius = 0.05;        //领域半径
	private double tabuR = 0.008;      //禁忌半径
	
	//初始化过程中产生的解的个数
	private static final int pop = 100;
	//当前解
	private double[] current = null;
	//混沌迭代次数
	private static final int K1 = 80;
	private static final int K2 = 10;
	
	//禁忌点
	private double[] tabuList = null;
	
	
	//禁忌表是否被初始化
	private boolean isInitialize = false;
	//对称取值范围
	private double rangeL = -1;
	private double rangeR = 4;
	private double range = rangeR - rangeL;
	
	private double step = range;

	TestFunction tst = new TestFunction();
	/**
	 * 初始化
	 */
	public LastImprovedCTSWithStages(int type)
	{
		fType =type;
		switch(fType){
		case 1: {
			dimensions = 30;
			rangeL = -1.5;
			rangeR = 1.5;
			range = rangeR - rangeL;
			break;
		}
		case 2: {
			//好结果是120维
			dimensions = 120;
			rangeL = -30;
			rangeR = 30;
			range = rangeR - rangeL;
			break;
		}
		case 3: {
			dimensions = 200;
			//rangeL = -32;
			//rangeR = 32;
			rangeL = -32.768;
			rangeR = 32.768;
			range = rangeR - rangeL;
			break;
		}
		case 4: {
			dimensions = 2;
			break;
		}
		case 5:{
			dimensions = 6;
			rangeL = -36;
			rangeR = 36;
			range = rangeR - rangeL;
			break;
		}
		case 6:{
			//好结果是3
			dimensions = 60;
			rangeL = -5.12;
			rangeR = 5.12;
			range = rangeR - rangeL;
			break;
		}
		case 7:{
			dimensions = 2;
			rangeL = -5;
			rangeR = 5;
			range = rangeR - rangeL;
			break;
		}
		case 8:{
			dimensions = 2;
			rangeL = -10;
			rangeR = 10;
			range = rangeR - rangeL;
			break;
		}
		case 9:{
			dimensions = 2;
	        rangeL = -30;
	        rangeR = 30;
	        range = rangeR - rangeL;
			break;
		}
		case 10:{
			dimensions = 20;
			rangeL = -1;
			rangeR = 4;
			range = rangeR - rangeL;
			break;
		}
		case 11:
		case 12:{
			dimensions = 20;
			rangeL = -1;
			rangeR = 4;
			range = rangeR - rangeL;
			break;
		}
		case 13:{
			dimensions = 100;
			rangeL = -600;
			rangeR = 600;
			range = rangeR - rangeL;
			break;
		}
		}
		current = new double[dimensions];
		tabuList =new double[dimensions];
	}
	/**
	 * 从源src(source)複製到目標tar(target)
	 * @param src
	 * @param tar
	 */
	void copy(double[] src, double[] tar)
	{
		for(int i = 0; i < src.length; ++ i)
			tar[i]  = src[i];
	}
	
	
	/**
	 * 求解目標函數的值
	 * @param a
	 * @return
	 */
	double f(double[] a)
	{
		return tst.cal(fType, a, a.length);
	}
	
	
	
	/**
	 * 利用正弦混沌來求得到初始解
	 */
	void iniWithSineMaps()
	{
		Random rand = new Random();
		double[] x = new double[dimensions];
		double[] y = new double[pop];
		double[][] ini = new double[pop][dimensions];
		
		for (int j = 0; j < pop; ++j) {
			//重新初始化
			for(int i = 0; i < dimensions; ++ i)
			{
				x[i] = rand.nextDouble();
			}
			//进行混沌迭代产生一组解
			for (int k = 0; k < dimensions; ++k) {
				int chaotic = 0;
				while (chaotic++ < K1) {
					x[k] =  Math.sin(x[k]);
				}
				ini[j][k] = x[k] * range + rangeL;
			}
			//计算适应度值
			y[j] = f(ini[j]);
		}
		
		int min = 0;
		for(int m = 1; m < pop; ++ m)
		{
			if(y[m] < y[min])    min = m;
		}
		copy(ini[min], current);
	}
	/**
	 * 根据给定的搜索阶段生成領域
	 * @param src
	 * @param tar
	 * @param stage
	 */
	
	void releaseRadius(boolean flag)
	{
		if(flag == true) radius = radius * 2;
		else radius = radius / 4;
		tabuR = radius / 100;
	}

	/**
	 * 判斷某個領域解是否在禁忌表中
	 * @param a
	 * @return
	 */
	boolean inTB(double[] a, int currentT)
	{
		if(isInitialize == false) return false;
		
		//double tabu = tabuR - currentT * decrease1;
		double tabu = tabuR;
		double distance = 0.0;
		for(int i = 0; i < dimensions; ++ i ){
			distance += (current[i] - a[i]) * (current[i] - a[i]);
		}
		if(distance > tabu * tabu) return false;
		else return true;
	}
	/**
	 * 將某個領域解加入到禁忌表中，同時更新已被禁忌的解的長度
	 * @param a
	 */
	void addTB(double[] a)
	{
		if(isInitialize == false) isInitialize = true;
		copy(current, tabuList);
	}
	void generateNeighbors(double[] src, double[]  tar, boolean is_improved)
	{
		Random rand = new Random();
		double square = 0;
		for(int i = 0; i < dimensions; ++ i){
			tar[i] = range * rand.nextDouble() + rangeL;
			
			square += (tar[i] - src[i]) * (tar[i] - src[i]);
		}
		if(square > radius * radius){
			for(int j = 0; j < dimensions; ++ j)
				tar[j] = src[j] - radius * (src[j] - tar[j]) / Math.abs(src[j] - tar[j]);
		}
 	}
	/**
	 * 根據給定的stage，进行阶段性搜索
	 * @param stage
	 */
	void stageSearch(final int stages)
	{
		//show();
		int maxN = neighbors;
		
		//定义当前迭代次数
		int currentT = 0;
		//定义领域
		double[]  nei = new double[dimensions];
		//当前最好的结果
		double bestResult = f(current);
		//没有改进次数
		int repeat = 0;
		double[] local_best_arr = new double[dimensions];
		double testD = 0;
		
		while(currentT < maxcycle)
		{
			//搜索出来的结果
			double localResult = Double.MAX_VALUE;
			int nn = 0;
			boolean flag = false;
			double local_best = bestResult;
			testD = 0;

			while (nn < maxN) {
				//如果好用就一直用,如果是true则随机性减弱，集中性增强，反之，亦然
				if(testD > 0){ 
					
					generateNeighbors(current, nei, true);
				}
				else{
					generateNeighbors(current,  nei, false);
				}
				//第一步判断是否在禁忌区域内
				if (inTB(nei, currentT) == false) {
					localResult = f(nei);
					//当前结果好于最好结果进行替换
					if (localResult <= bestResult) {
						if(flag == false) testD = bestResult - localResult;
						addTB(current);       // 将原来的路径加入禁忌表中
						copy(nei, current);  // 将当前路径作为最好路径
						bestResult = localResult; // 将当前结果作为最好的结果
						flag = true;
					}
				}
				//在禁忌区中，选择在这个循环中所有被禁忌对象中结果最好的，以便最后进行赦免
				else{
					if(local_best > localResult) {
						copy(nei, local_best_arr);
						local_best = localResult;
					}
				}
				++nn;                  
			}
			//如果此次循环没有任何改进且禁忌列表中的结果好于最好的结果则进行赦免
			if(flag == false) { //赦免                     
				if(local_best < bestResult){
					testD = bestResult - local_best;
					copy(local_best_arr, current);
					isInitialize = false;
				}
			}
			else{
				repeat = 0;
			}
			double measure = testD / bestResult;
			
			if(measure < 10E-6){
				++ repeat;
			}
			//重复十次则松绑一次
			if(repeat == 10) {
				releaseRadius(false);
			}
			if(repeat == 15){
				releaseRadius(true);
			}
			
			System.out.println("Radius: " + radius);
			System.out.println("testD: " + testD);
			show();
			System.out.println();
			
			//在这个地方体现了奖励策略，多给几次循环
			if(measure > 0.1) currentT = currentT - 2;
			else if(measure > 0.01) currentT = currentT - 1;
			else if(measure > 0.001) currentT = currentT;
			else ++ currentT;
			
			
		//	else -- currentT;
			//++currentT;
		}

	}
	
	void show()
	{
		double result = 0.0;
		for(int i = 0; i < dimensions; ++ i)
			System.out.print(current[i]  + "   ");
		System.out.println("");
		result = f(current);
		System.out.println("Result: " + result);
	}
	void solve()
	{
		iniWithSineMaps();
		System.out.println("$$$$$$$$$$$$$$$$$$ini#############");
		show();
		
		int  stages = 5;
		tabuR  = radius = range;
		tabuR = radius / 100;
		neighbors = 20;
		maxcycle = 30;
		
		show();
		radius = range / 2;
		tabuR = radius / 100;
		
		for(int i = 0; i < stages; ++ i){
			neighbors += stages *3;
			maxcycle += stages * 3;
			System.out.println("########################   " + i + "$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
			System.out.println("#############radius:" + radius + "$$$$$$$$$maxcycle:" + maxcycle);
			
			stageSearch(i);
		}
	}
	
	public static void main(String[] args)
	{
		long start = System.currentTimeMillis();
		/*
		 * 1--30  	2--120 	3--200 	4--2 5--6  6 --60 7--2      8--2 MS     9 -- 2 branin    10 --20 Brown3 11--20Brown3
		 *4稍微不行，6肯定不行，11非常不稳，12稳定性强一些
		 */
		//if(args == null) return;
		//int type = Integer.parseInt(args[0]);
		int type = 12;
		LastImprovedCTSWithStages ss = new LastImprovedCTSWithStages(type);
		/*
		Timer  timer  = new Timer();
		timer.schedule(new TimerTask(){
			public void run(){
			System.exit(0);
		}
		}, 900);
		*/
	    ss.solve();
	    long end = System.currentTimeMillis();
	    //ss.show();
	    System.out.println();
	    System.out.println("function type" + type);
		System.out.println( end -start);
	}
	void improveSearch(int times)
	{
		System.out.println("********************调用improveSearch***************************");
		double step = range;
		int currentT = 0;
		double[][] nei = new double[neighbors][dimensions];
		
		while(currentT ++ < times)
		{
			double best = f(current);
			boolean flag = false;
			for(int i = 0; i < neighbors; ++ i){
				copy(current, nei[i]);
				for (int j = 0; j < dimensions; ++j) {
					double temp = nei[i][j];
					nei[i][j] += step;
					double devi = ( f(nei[i]) - f(current) ) / f(current);
					nei[i][j] = devi * radius + current[j];
					
					if(inTB(nei[i], 0) == false && f(nei[i]) < best){
						best = f(nei[i]);
						addTB(current);
						copy(nei[i], current);
						flag = true;
					}
					else nei[i][j] = temp;
				}
			}//end of for loop
			if(flag == true){
				step = step / 2;
			}
		}
	}
}








































































