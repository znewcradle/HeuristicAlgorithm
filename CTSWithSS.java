package tabooSearch;

import java.util.Random;


/**
 * 利用正弦混沌和分阶段计算策略来改善连续禁忌搜索的全局搜索性能
 * @author Yunjia Xu
 *
 */
public class CTSWithSS {
	//目标解的维度
	private static  int dimensions = 2;
	//转化定义域参数
	private static double factor = 1;
	//第一阶段的领域个数
	private static final int m1 = 24;     
	//第一阶段的领域半径
	private static final double R1 = 0.05;
	//第一阶段的迭代次数
	private static final int maxcycle1 = 40;
	//第二阶段的领域个数
	private static final int m2 = 32;
	//第二阶段的领域半径
	private static final double R2 = 0.01;
	//第二阶段的迭代次数
	private static final int maxcycle2 = 80;
	//初始化过程中产生的解的个数
	private static final int pop = 60;
	//当前解
	private double[] current = new double[dimensions];
	//混沌迭代次数
	private static final int K = 40;
	
	//禁忌点
	private double[] tabuList = new double[dimensions];
	//禁忌半径
	private double tabuR = 0.008;
	//禁忌表是否被初始化
	private boolean isInitialize = false;
	
	/**
	 * 初始化
	 */
	public CTSWithSS()
	{
		if(dimensions == 30) factor = 1.5;
		else if(dimensions == 120) factor = 0.1;
		else if(dimensions == 2) factor = 10;
		else factor = 1;
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
	
	class testFunction{
		double factor = 1.0;
		public testFunction(double f)
		{
			factor = f;
		}
		public double f1(double[] a)
		{
			double result = 0.0;
			for(int i = 0; i < a.length; ++ i)
			{
				double tran = factor * a[i];
				result += tran * tran;
			}
			return result;
		}
		public double f2(double[] a)
		{
			double result = 0.0;
			double temp = 0.0;
			for(int i = 0; i < a.length; ++ i)
			{
				temp = 0.0;
				for(int j = 0; j <= i; ++j)
				{
					temp += a[i] * factor;
				}
				result += temp * temp;
			}
			return result;
		}
		public double f3(double[] a)
		{
			double result = 0.0;
			double left = 0.0;
			double right = 0.0;
			
			for(int i = 0; i < a.length; ++ i)
			{
				left += a[i] * a[i] * factor * factor;
				right += Math.cos(2 * a[i] * Math.PI * factor);
			}
			left  = -0.2 * Math.sqrt(left / a.length);
			left = -20 * Math.exp(left);
			right = Math.exp(right / a.length);
			result = left - right + 20 + Math.exp(1.0);
			return result;
		}
		public double f4(double[] a)
		{
			double result = 0.0;
			double up = 0.0;
			double down = 0.0;
			up = Math.sin(Math.sqrt(a[0] * a[0] * factor * factor + a[1] * a[1] * factor * factor));
			down = 1 +  0.001 * (a[0] * a[0] * factor * factor + a[1] * a[1] * factor * factor);
			result = 0.5 + (up * up - 0.5) / (down * down);
			return result;
		}
	}
	
	/**
	 * 求解目標函數的值
	 * @param a
	 * @return
	 */
	double f(double[] a)
	{
		testFunction tst = new testFunction(factor);
		if(dimensions == 30) return tst.f1(a);
		else if(dimensions == 120) return tst.f2(a);
		else if(dimensions == 200) return tst.f3(a);
		else return tst.f4(a);
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
				x[i] = 2.0 * rand.nextDouble() / 100 - 0.01;
			}
			//进行混沌迭代产生一组解
			for (int k = 0; k < dimensions; ++k) {
				int chaotic = 0;
				while (chaotic++ < K) {
					x[k] = Math.sin(x[k] * Math.PI);
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
		for(int i = 0; i < dimensions; ++ i){
			tar[i] = 2 * rand.nextDouble() - 1;
			
			int chaotic = 0;
			while(chaotic ++ < K){
				tar[i] = Math.sin(tar[i] * Math.PI);
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
		}
	}
	void test()
	{
		double result = 0.0;
		for(int i = 0; i < dimensions; ++ i)
			System.out.print(current[i]* factor  + "   ");
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
		CTSWithSS ss = new CTSWithSS();
	    ss.solve();
	    long end = System.currentTimeMillis();
	    System.out.println();
	    System.out.println(end - start);
	}
}





































