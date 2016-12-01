package tabooSearch;

import java.util.Random;


/**
 * �������һ���ͷֽ׶μ��������������������������ȫ����������
 * @author Yunjia Xu
 *
 */
public class CTSWithSS {
	//Ŀ����ά��
	private static  int dimensions = 2;
	//ת�����������
	private static double factor = 1;
	//��һ�׶ε��������
	private static final int m1 = 24;     
	//��һ�׶ε�����뾶
	private static final double R1 = 0.05;
	//��һ�׶εĵ�������
	private static final int maxcycle1 = 40;
	//�ڶ��׶ε��������
	private static final int m2 = 32;
	//�ڶ��׶ε�����뾶
	private static final double R2 = 0.01;
	//�ڶ��׶εĵ�������
	private static final int maxcycle2 = 80;
	//��ʼ�������в����Ľ�ĸ���
	private static final int pop = 60;
	//��ǰ��
	private double[] current = new double[dimensions];
	//�����������
	private static final int K = 40;
	
	//���ɵ�
	private double[] tabuList = new double[dimensions];
	//���ɰ뾶
	private double tabuR = 0.008;
	//���ɱ��Ƿ񱻳�ʼ��
	private boolean isInitialize = false;
	
	/**
	 * ��ʼ��
	 */
	public CTSWithSS()
	{
		if(dimensions == 30) factor = 1.5;
		else if(dimensions == 120) factor = 0.1;
		else if(dimensions == 2) factor = 10;
		else factor = 1;
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
	 * ���Ŀ�˺�����ֵ
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
				x[i] = 2.0 * rand.nextDouble() / 100 - 0.01;
			}
			//���л����������һ���
			for (int k = 0; k < dimensions; ++k) {
				int chaotic = 0;
				while (chaotic++ < K) {
					x[k] = Math.sin(x[k] * Math.PI);
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
		CTSWithSS ss = new CTSWithSS();
	    ss.solve();
	    long end = System.currentTimeMillis();
	    System.out.println();
	    System.out.println(end - start);
	}
}





































