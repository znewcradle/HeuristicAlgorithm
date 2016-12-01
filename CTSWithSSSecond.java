package tabooSearch;

import java.util.Random;
import pso.TestFunction;

/**
 * �������һ���ͷֽ׶μ��������������������������ȫ����������
 * @author Yunjia Xu
 *
 */
public class CTSWithSSSecond {
	//ѡ����������
	/*
	 * 1--30  	2--120 	3--200 	4--2
	 */
	private final  int fType = 2;
	//Ŀ����ά��
	private static  int dimensions = 120;
	//��һ�׶ε��������
	private static final int m1 = 40;     
	//��һ�׶ε�����뾶
	private static final double R1 = 20;
	//��һ�׶εĵ�������
	private static final int maxcycle1 = 40;
	//�ڶ��׶ε��������
	private static final int m2 = 80;
	//�ڶ��׶ε�����뾶
	private static final double R2 = 10;
	//�ڶ��׶εĵ�������
	private static final int maxcycle2 = 80;
	//��ʼ�������в����Ľ�ĸ���
	private static final int pop = 80;
	//��ǰ��
	private double[] current = null;
	//�����������
	private static final int K1 = 40;
	private static final int K2 = 30;
	
	//���ɵ�
	private double[] tabuList = null;
	//���ɰ뾶
	private double tabuR = 1;
	//���ɱ��Ƿ񱻳�ʼ��
	private boolean isInitialize = false;
	//�Գ�ȡֵ��Χ
	private double range = 10000;
	//ÿ����������ݼ��İ뾶
	private double decrease = 0.01;
	
	/**
	 * ��ʼ��
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
	 * ��Դsrc(source)�}�u��Ŀ��tar(target)
	 * @param src
	 * @param tar
	 */
	void copy(double[] src, double[] tar)
	{
		for(int i = 0; i < src.length; ++ i)
			tar[i]  = src[i];
	}
	
	
	/**
	 * ���Ŀ�˺�����ֵ
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
	 * �������һ������õ���ʼ��
	 */
	void iniWithSineMaps()
	{
		Random rand = new Random();
		double[] x = new double[dimensions];
		double[] y = new double[pop];
		double[][] ini = new double[pop][dimensions];
		
	
		for (int j = 0; j < pop; ++j) {
			//���³�ʼ��
			for(int i = 0; i < dimensions; ++ i)
			{
				x[i] = 2.0 * range * rand.nextDouble() - range;
			}
			//���л����������һ���
			for (int k = 0; k < dimensions; ++k) {
				int chaotic = 0;
				while (chaotic++ < K1) {
					x[k] = range * Math.sin(x[k] * Math.PI / range);
				}
				ini[j][k] = x[k];
			}
			//������Ӧ��ֵ
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
	 * ���ݸ����������A�������I��
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
	 * �Д�ĳ���I����Ƿ��ڽ��ɱ���
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
	 * ��ĳ���I�����뵽���ɱ��У�ͬ�r�����ѱ����ɵĽ���L��
	 * @param a
	 */
	void addTB(double[] a)
	{
		if(isInitialize == false) isInitialize = true;
		copy(current, tabuList);
	}
	/**
	 * �����o����stage���M���A���Ե��I������
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
		//���嵱ǰ��������
		int currentT = 0;
		//��������
		double[]  nei = new double[dimensions];
		//��ǰ��õĽ��
		double bestResult = f(current);
		
		while(currentT < maxcycle)
		{
			//���������Ľ��
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
		//���һ����ʼ��
		iniWithSineMaps();
		//��һ�׶�����
		stageSearch(1);
		//�ڶ��׶�����
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





































