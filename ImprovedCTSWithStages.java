package pso;

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
public class ImprovedCTSWithStages {
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
	//递减因子
	private double decrease1 = radius / 100;
	private double decrease2 = tabuR / 100;
	

	TestFunction tst = new TestFunction();
	/**
	 * 初始化
	 */
	public ImprovedCTSWithStages(int type)
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
			rangeL = -10;
			rangeR = 10;
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
				x[i] = range * rand.nextDouble() + rangeL;
			}
			//进行混沌迭代产生一组解
			for (int k = 0; k < dimensions; ++k) {
				int chaotic = 0;
				while (chaotic++ < K1) {
					x[k] = range * Math.sin(( x[k] - rangeL) * Math.PI / range) + rangeL;
					//x[k] = range * Math.sin( x[k] * Math.PI / range) + rangeL;
				}
				ini[j][k] = x[k];
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
	 * 根据给定的搜索階段生成領域
	 * @param src
	 * @param tar
	 * @param stage
	 */
	
	void generateNeighbors(double[] src, double[]  tar, int currentT)
	{
		Random rand = new Random();
		for(int i = 0; i < dimensions; ++ i){
			
			tar[i] = 2.0 * range * rand.nextDouble() - range;
			
			int chaotic = 0;
			while(chaotic ++ < K2){
				tar[i] = range * Math.sin(tar[i] * Math.PI / range);
			}
			
			if(Math.abs(src[i] - tar[i]) > radius){
				tar[i] = src[i] - radius * (src[i] - tar[i]) / Math.abs(src[i] - tar[i]);
			}
			if(tar[i] < rangeL) tar[i] = rangeL;
			if(tar[i] > rangeR) tar[i] = rangeR;
 		}
	}
	void releaseRadius(boolean flag)
	{
		if(flag == true) radius = radius * 2;
		else radius = radius / 2;
		tabuR = radius / 100;
		decrease1 = tabuR / 100;
		decrease2 = radius / 100;
	}

	/**
	 * 判斷某個領域解是否在禁忌表中
	 * @param a
	 * @return
	 */
	boolean inTB(double[] a, int currentT)
	{
		if(isInitialize == false) return false;
		
		double tabu = tabuR - currentT * decrease1;
		//double tabu = tabuR;
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
	/**
	 * 根據給定的stage，進行階段性的領域搜索
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
				generateNeighbors(current, nei, currentT);
				if (inTB(nei, currentT) == false) {
					localResult = f(nei);
					if (localResult <= bestResult) {
						if(flag == false) testD = bestResult - localResult;
						addTB(current);       // 将原来的路径加入禁忌表中
						copy(nei, current);  // 将当前路径作为最好路径
						bestResult = localResult; // 将当前结果作为最好的结果
						flag = true;
					}
				}
				else{
					if(local_best > localResult) {
						copy(nei, local_best_arr);
						local_best = localResult;
					}
				}
				++nn;
			}
			if(flag == false) { //赦免
				++ repeat;
				if(local_best < bestResult){
					testD = bestResult - local_best;
					copy(local_best_arr, current);
				}
			}else{
				repeat = 0;
				//if(stages == 4)    releaseRadius(false);
				//releaseRadius(false);
			}
			if(repeat == 10) {
				releaseRadius(true);
			}
			//if(repeat == Maxkk) improveSearch(20);
			System.out.println("testD: " + testD);
			show();
			System.out.println();
			
		//	if(testD < 0.0001)  	currentT = currentT + 1;
			
			
			//if(testD / 1 > 1) currentT = currentT - 3;
			if(testD / 0.1 > 1) currentT = currentT - 2;
			else if(testD / 0.01 > 1) currentT = currentT - 1;
			else if(testD / 0.001 > 1) currentT = currentT;
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
		
		int  stages = 6;
		tabuR  = radius = range;
		tabuR = radius / 100;
		neighbors = 20;
		maxcycle = 10;
		
		show();
		for(int i = 0; i < stages; ++ i){
			radius = radius / ( 4 * ( i + 2));
			tabuR = radius / 100;
			System.out.println("########################   " + i + "$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
			neighbors += stages *3;
			maxcycle += stages * 3;
			
			decrease1 = tabuR / 100;
			decrease2 = radius / 100;
			stageSearch(i);
		}
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
	public static void main(String[] args)
	{
		long start = System.currentTimeMillis();
		/*
		 * 1--30  	2--120 	3--200 	4--2 5--6  6 --60 7--2      8--2 MS     9 -- 2 branin    10 --20 Brown3 11--20Brown3
		 *目前是1，7，5都没有丝毫问题；3容易陷入局部最优，但是可以算出好结果；2，6算不出好结果
		 */
		//if(args == null) return;
		//int type = Integer.parseInt(args[0]);
		int type = 13;
		ImprovedCTSWithStages ss = new ImprovedCTSWithStages(type);
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
}








































































