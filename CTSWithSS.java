package tabooSearch;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import pso.TestFunction;


/**
 * �������һ���ͷֽ׶μ��������������������������ȫ����������
 * @author Yunjia Xu
 *
 */
public class CTSWithSS {
	//Ŀ����ά��
	private static  int dimensions = 2;
	//��һ�׶ε��������
	private static final int m1 = 36;     
	//��һ�׶ε�����뾶
	private static final double R1 = 0.05;
	//��һ�׶εĵ�������
	private static final int maxcycle1 = 120;
	//�ڶ��׶ε��������
	private static final int m2 = 54;
	//�ڶ��׶ε�����뾶
	private static final double R2 = 0.01;
	//�ڶ��׶εĵ�������
	private static final int maxcycle2 = 80;
	//��ʼ�������в����Ľ�ĸ���
	private static final int pop = 80;
	//��ǰ��
	private double[] current;
	//�����������
	private static final int K = 160;
	
	//���ɵ�
	private double[] tabuList;
	//���ɰ뾶
	private double tabuR = 0.01;
	//���ɱ��Ƿ񱻳�ʼ��
	private boolean isInitialize = false;
	//���Ժ�������
	private int fType = 0;
	//ȡֵ��Χ
	private double rangeL = -1.5;
	private double rangeR = 1.5;
	private double range =  3;
	
	TestFunction tst = new TestFunction();
	/**
	 * ��ʼ��
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
			//�ý����120ά
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
			//�ý����3
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
		return tst.cal(fType, a, a.length);
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
				x[i] = range * rand.nextDouble() + rangeL;
			}
			//���л����������һ���
			for (int k = 0; k < dimensions; ++k) {
				int chaotic = 0;
				while (chaotic++ < K) {
					x[k] = range * Math.sin((x[k] - rangeL) * Math.PI / range) + rangeL;
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
	 * �Д�ĳ���I����Ƿ��ڽ��ɱ���
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





































