package tabooSearch;

import java.util.Random;
import pso.TestFunction;

/**
 * 利用正弦混沌和分阶段计算策略来改善连续禁忌搜索的全局搜索性能
 * @author Yunjia Xu
 *
 */
public class CTSWithSSSecond {
	//选择函数的类型
	/*
	 * 1--30  	2--120 	3--200 	4--2
	 */
	private final  int fType = 2;
	//目标解的维度
	private static  int dimensions = 120;
	//第一阶段的领域个数
	private static final int m1 = 40;     
	//第一阶段的领域半径
	private static final double R1 = 20;
	//第一阶段的迭代次数
	private static final int maxcycle1 = 40;
	//第二阶段的领域个数
	private static final int m2 = 80;
	//第二阶段的领域半径
	private static final double R2 = 10;
	//第二阶段的迭代次数
	private static final int maxcycle2 = 80;
	//初始化过程中产生的解的个数
	private static final int pop = 80;
	//当前解
	private double[] current = null;
	//混沌迭代次数
	private static final int K1 = 40;
	private static final int K2 = 30;
	
	//禁忌点
	private double[] tabuList = null;
	//禁忌半径
	private double tabuR = 1;
	//禁忌表是否被初始化
	private boolean isInitialize = false;
	//对称取值范围
	private double range = 10000;
	//每次搜索领域递减的半径
	private double decrease = 0.01;
	
	/**
	 * 初始化
	 */
	public CTSWithSSSecond()
	{
		switch (fType) {
		case 1: {
			dimensions = 30;
			break;
		}
		case 2: {
			dimensions = 120;
			break;
		}
		case 3: {
			dimensions = 200;
			break;
		}
		case 4: {
			dimensions = 2;
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
		TestFunction tst = new TestFunction();
		if(dimensions == 30) return tst.f1(a, a.length);
		else if(dimensions == 120) return tst.f2(a, a.length);
		else if(dimensions == 200) return tst.f3(a, a.length);
		else return tst.f4(a, a.length);
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
				x[i] = 2.0 * range * rand.nextDouble() - range;
			}
			//进行混沌迭代产生一组解
			for (int k = 0; k < dimensions; ++k) {
				int chaotic = 0;
				while (chaotic++ < K1) {
					x[k] = range * Math.sin(x[k] * Math.PI / range);
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
	void generateNeighbors(double[] src, double[]  tar, int stage, int nn)
	{
		double radius = R1 - nn * decrease;
		if(stage == 2) radius = R2 - nn * decrease;
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
		boolean flag = true;
		for(int i = 0; i < dimensions; ++ i ){
			if(Math.abs(tabuList[i] - a[i]) > tabuR) {
				flag = false;
				break;
			}
		}
		return flag;
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
	void stageSearch(int stage)
	{
		test();
		if(stage != 1 && stage != 2) return;
		int maxcycle = maxcycle1;
		int maxN = m1;
		if(stage == 2){
			maxcycle = maxcycle2;
			maxN = m2;
			tabuR = tabuR / 8;
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
				generateNeighbors(current, nei, stage, nn);
				if(inTB(nei) == false){
				   localResult = f(nei);
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
			if(currentT % 3 == 0) ++ maxN;
		}
	}
	void test()
	{
		double result = 0.0;
		for(int i = 0; i < dimensions; ++ i)
			System.out.print(current[i]  + "   ");
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
		CTSWithSSSecond ss = new CTSWithSSSecond();
	    ss.solve();
	    long end = System.currentTimeMillis();
	    System.out.println();
	    System.out.println(end - start);
	}
}





































