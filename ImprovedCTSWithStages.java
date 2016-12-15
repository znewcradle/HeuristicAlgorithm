package pso;

import java.util.Random;
import pso.TestFunction;

/**
 * �������һ���ͷֽ׶μ��������������������������ȫ����������
 * @author Yunjia Xu
 *
 */
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import pso.TestFunction;

/**
 * �������һ���ͷֽ׶μ��������������������������ȫ����������
 * @author Yunjia Xu
 *
 */
public class ImprovedCTSWithStages {
	//ѡ����������
	/*
	 * 1--30  	2--120 	3--200 	4--2 5--6  6 --60 7--2
	 */
	private int fType = 6;
	//Ŀ����ά��
	private static  int dimensions = 2;
	
	//��ʼ�Ĳ�������
	private int neighbors = 32;				//�������
	private int maxcycle  = 80;				//����������,ԭ����80
	private double radius = 0.05;        //����뾶
	private double tabuR = 0.008;      //���ɰ뾶
	
	//��ʼ�������в����Ľ�ĸ���
	private static final int pop = 100;
	//��ǰ��
	private double[] current = null;
	//�����������
	private static final int K1 = 80;
	private static final int K2 = 10;
	
	//���ɵ�
	private double[] tabuList = null;
	
	
	//���ɱ��Ƿ񱻳�ʼ��
	private boolean isInitialize = false;
	//�Գ�ȡֵ��Χ
	private double rangeL = -1;
	private double rangeR = 4;
	private double range = rangeR - rangeL;
	//�ݼ�����
	private double decrease1 = radius / 100;
	private double decrease2 = tabuR / 100;
	

	TestFunction tst = new TestFunction();
	/**
	 * ��ʼ��
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
			//�ý����120ά
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
				while (chaotic++ < K1) {
					x[k] = range * Math.sin(( x[k] - rangeL) * Math.PI / range) + rangeL;
					//x[k] = range * Math.sin( x[k] * Math.PI / range) + rangeL;
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
	 * �Д�ĳ���I����Ƿ��ڽ��ɱ���
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
	void stageSearch(final int stages)
	{
		//show();
		int maxN = neighbors;
		
		//���嵱ǰ��������
		int currentT = 0;
		//��������
		double[]  nei = new double[dimensions];
		//��ǰ��õĽ��
		double bestResult = f(current);
		//û�иĽ�����
		int repeat = 0;
		double[] local_best_arr = new double[dimensions];
		double testD = 0;
		
		while(currentT < maxcycle)
		{
			//���������Ľ��
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
						addTB(current);       // ��ԭ����·��������ɱ���
						copy(nei, current);  // ����ǰ·����Ϊ���·��
						bestResult = localResult; // ����ǰ�����Ϊ��õĽ��
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
			if(flag == false) { //����
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
		System.out.println("********************����improveSearch***************************");
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
		 *Ŀǰ��1��7��5��û��˿�����⣻3��������ֲ����ţ����ǿ�������ý����2��6�㲻���ý��
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








































































