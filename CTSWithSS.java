package tabooSearch;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import pso.TestFunction;


/**
 * 利用正弦混沌和分阶段计算策略来改善连续禁忌搜索的全局搜索性能
 * @author Yunjia Xu
 *
 */
public class CTSWithSS {
	//目标解的维度
	private static  int dimensions = 2;
	//第一阶段的领域个数
	private static final int m1 = 36;     
	//第一阶段的领域半径
	private static final double R1 = 0.05;
	//第一阶段的迭代次数
	private static final int maxcycle1 = 120;
	//第二阶段的领域个数
	private static final int m2 = 54;
	//第二阶段的领域半径
	private static final double R2 = 0.01;
	//第二阶段的迭代次数
	private static final int maxcycle2 = 80;
	//初始化过程中产生的解的个数
	private static final int pop = 80;
	//当前解
	private double[] current;
	//混沌迭代次数
	private static final int K = 160;
	
	//禁忌点
	private double[] tabuList;
	//禁忌半径
	private double tabuR = 0.01;
	//禁忌表是否被初始化
	private boolean isInitialize = false;
	//测试函数类型
	private int fType = 0;
	//取值范围
	private double rangeL = -1.5;
	private double rangeR = 1.5;
	private double range =  3;
	
	TestFunction tst = new TestFunction();
	/**
	 * 初始化
	 */
	public CTSWithSS(int type)
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
			rangeL = -32;
			rangeR = 32;
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
			rangeL = -10;
			rangeR = 10;
			range = rangeR - rangeL;
			break;
		}
		}
		current = new double[dimensions];
		tabuList = new double[dimensions];
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
				while (chaotic++ < K) {
					x[k] = range * Math.sin((x[k] - rangeL) * Math.PI / range) + rangeL;
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
	void generateNeighbors(double[] src, double[]  tar, int stage)
	{
		double radius = R1;
		if(stage == 2) radius = R2;
		Random rand = new Random();
		double result = 0.0;
		
		for(int i = 0; i < dimensions; ++ i){
			tar[i] = radius * ( 2 * rand.nextDouble() - 1) + src[i];
			
			int chaotic = 0;
			while(chaotic ++ < K){
				tar[i] = radius * Math.sin( ( tar[i] - src[i]) * Math.PI / radius / 2) + src[i];
			}
			
			result += (tar[i] - src[i]) * (tar[i] - src[i]);
		}
		if(result > radius * radius){
			for(int i  = 0; i < dimensions; ++ i){
				tar[i] = src[i] - radius * (src[i] - tar[i]) / Math.abs(src[i] - tar[i]);
			}
		}
		
	}

	/**
	 * 判斷某個領域解是否在禁忌表中
	 * @param a
	 * @return
	 */
	boolean inTB(double[] a)
	{
        if(isInitialize == false) return false;
		
		double distance = 0.0;
		for(int i = 0; i < dimensions; ++ i ){
			distance += (current[i] - a[i]) * (current[i] - a[i]);
		}
		if(distance > tabuR * tabuR) return false;
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
	void stageSearch(final int stage)
	{
		test();
		if(stage != 1 && stage != 2) return;
		int maxcycle = maxcycle1;
		int maxN = m1;
		if(stage == 2){
			maxcycle = maxcycle2;
			maxN = m2;
		}
		System.out.println("#####################Stage: "  + stage + "$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
		//定义当前迭代次数
		int currentT = 0;
		//定义领域
		double[]  nei = new double[dimensions];
		//当前最好的结果
		double bestResult = f(current);
		
		while(currentT < maxcycle)
		{
			//搜索出来的结果
			double localResult = Double.MAX_VALUE;
		
			int nn = 0;
			while(nn < maxN){
				generateNeighbors(current, nei, stage);
				localResult = f(nei);
				if(inTB(nei) == false){
					if(localResult <= bestResult){
						addTB(current);
						copy(nei, current);
						bestResult = localResult;
					}
				}
				++ nn;
			}
			addTB(current);
			test();
			++ currentT;
		}
	}
	void test()
	{
		double result = 0.0;
		for(int i = 0; i < dimensions; ++ i)
			System.out.print(current[i]);
		System.out.println("");
		result = f(current);
		System.out.println("Result: " + result);
		System.out.println("");
	}
	void solve()
	{
		//正弦混沌初始化
		iniWithSineMaps();
		//第一阶段搜索
		stageSearch(1);
		//第二阶段搜索
		stageSearch(2);
	}
	
	public static void main(String[] args)
	{
		long start = System.currentTimeMillis();
		if(args == null) return;
		//int type = Integer.parseInt(args[0]);
		int type = 1;
		CTSWithSS ss = new CTSWithSS(type);
		
		Timer  timer  = new Timer();
		timer.schedule(new TimerTask(){
			public void run(){
			System.exit(0);
		}
		}, 900);
		
	    ss.solve();
	    long end = System.currentTimeMillis();
	    System.out.println();
	    System.out.println("function type" + type);
		System.out.println( end -start);
	}
}





































