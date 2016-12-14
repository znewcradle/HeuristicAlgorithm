package tabooSearch;

import java.util.Random;
import pso.TestFunction;

/**
 * �������һ���ͷֽ׶μ��������������������������ȫ����������
 * @author Yunjia Xu
 *
 */
public class CTSWithSSWithStagesPrev {
	//ѡ����������
	private int fType = 3;
	//Ŀ����ά��
	private static  int dimensions = 120;
	
	//��ʼ�Ĳ�������
	private int neighbors = 32;				//�������
	private int maxcycle  = 40;				//����������
	private double radius = 0.05;        //����뾶
	private double tabuR = 0.008;      //���ɰ뾶
	
	//��ʼ�������в����Ľ�ĸ���
	private static final int pop = 60;
	//��ǰ��
	private double[] current = null;
	//�����������
	private static final int K1 = 40;
	private static final int K2 = 30;
	
	//���ɵ�
	private double[] tabuList = null;
	
	
	//���ɱ��Ƿ񱻳�ʼ��
	private boolean isInitialize = false;
	//�Գ�ȡֵ��Χ
	private int range = 30;
	
	
	/**
	 * ��ʼ��
	 */
	public CTSWithSSWithStagesPrev()
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
		tabuList = new double[dimensions];
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
	void generateNeighbors(double[] src, double[]  tar)
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
	void stageSearch()
	{
		show();
		int maxN = neighbors;
		
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
				generateNeighbors(current, nei);
				if(inTB(nei) == false){
				   localResult = f(nei);
					if(localResult <= bestResult){
						addTB(current);                    //��ԭ����·��������ɱ���
						copy(nei, current);               //����ǰ·����Ϊ���·��
						bestResult = localResult;    //����ǰ�����Ϊ��õĽ��
					}
				}
				++ nn;
			}
			addTB(current);
			show();
			++ currentT;
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
		System.out.println("");
	}
	void solve()
	{
		//���һ����ʼ��
		iniWithSineMaps();
		/*
		 * private int neighbors = 32;				//�������
			private int maxcycle  = 40;				//����������
			private double radius = 0.05;        //����뾶
			private double tabuR = 0.008;      //���ɰ뾶
		 */
		/*
		 * 1--30  	2--120 	3--200 	4--2
		 */
		fType = 1;
		int  stages = 5;
		tabuR  = radius = range;
		neighbors = 20;
		maxcycle = 10;
		//double decrease = tabuR;
		//range ҪС��100;
		for(int i = 0; i < stages; ++ i){
			if(i == 0) radius = radius / 100;
			else if(i < 3) radius = radius / 10;
			else if(i < 4) radius = radius / 5;
			else radius = radius / 2;
			
			tabuR = radius / 10;
			System.out.println("########################   " + i + "$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
			neighbors += stages * 5;
			maxcycle += stages * 5;
			stageSearch();
		}
		
	}
	
	public static void main(String[] args)
	{
		long start = System.currentTimeMillis();
		CTSWithSSWithStagesPrev ss = new CTSWithSSWithStagesPrev();
	    ss.solve();
	    long end = System.currentTimeMillis();
	    System.out.println();
	    System.out.println(end - start);
	}
}





































