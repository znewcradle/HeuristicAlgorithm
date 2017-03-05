package tabooSearch;

import java.util.Random;

import pso.TestFunction;

public class MCTS {
	private int fType = 1;                                            //���Ժ���������
	private int dimensions = 30;                                //����ά��
	private double radius = 0.0;                                //����뾶
	private static final int maxcycle = 4000;             //���ĵ�������
	private int neighbors = 0;                                    //�������
	private int currentT = 0;                                      //��ǰ��������
	private int Maxkk = 50;                                       //����û���κθĽ��ĵ�������
	private double pp = 0.1;                                     //�ڵڶ��׶��������ˮ��
	private double por = 0.1;                                   //�����׶εĲ�����ˮ��
	private double step = radius;                            //ÿ���׶εĲ���
	//��ǰ��ѽ�
	private double[] current = null;
	//���ɳ���
	private int tabuLength = 5;
	//ȡֵ��Χ
	private double rangeL = -1;
	private double rangeR = 4;
	private double range = rangeR  - rangeL;
	private double min = 1;
	private double max = 1;
	//�����б�
	class Tabu{
		public double[] tabu = null;
		public Tabu next = null;
		public Tabu()
		{
			tabu = null;
			next = null;
		}
	}
	Tabu tabuList = new Tabu();
	
	
	public MCTS(int type){
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
		}
		current = new double[dimensions];
	}

	double f(double[] a)
	{
		TestFunction tst = new TestFunction();
		 switch(fType){
		 case 1: return tst.f1(a, a.length); 
		 case 2: return tst.f2(a, a.length);
		 case 3: return tst.f3(a, a.length);
		 case 4: return tst.f4(a, a.length);
		 case 5: return tst.f5(a, a.length);
		 case 6: return tst.f6(a, a.length);
		 case 7: return tst.f7(a, a.length);
		 case 8: return tst.f8(a, a.length);
		 case 9: return tst.f9(a, a.length);
		 case 10:return tst.f10(a, a.length);
		 }
		 return -1;
	}
	
	void initialization()
	{
		Random rand = new Random();
		for(int i = 0; i < dimensions; ++ i){
			current[i] =  rand.nextDouble() * range + rangeL;
		}
		/*
		int pop = 80;
		int K1= 30;
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
		*/
	}
	void copy(double[] src, double[] tar)
	{
		for(int i = 0; i < src.length; ++ i)
			tar[i]  = src[i];
	}
	boolean inTB(double[] a)
	{
		if(tabuList.tabu == null) return false;
		Tabu list = tabuList;
		for(int i = 0; i < tabuLength; ++ i){
			int j = 0;
			for(; j < dimensions; ++ j){
				if( list.tabu[j] ==  a[j] ) continue;
				else break;
			}
			if(j == dimensions) return true;
			list = tabuList.next;
			if(list == null) return false;
		}
		return false;
	}
	void addIntoTB(double[] a)
	{
		Tabu list = tabuList;
		int i = 0; 
		for( ;i < tabuLength; ++ i){
			if(list.tabu != null) {
				list = list.next;
				continue;
			}
			else{
				Tabu newItem = new Tabu();
				newItem.tabu = a;
				list.next = newItem;
				break;
			}
		}
		if(i == tabuLength){
			tabuList = tabuList.next;
			Tabu newItem = new Tabu();
			newItem.tabu = a;
			list.next = newItem;
		}
	}
	void firstStageSearch(){
		int repeat = 0;
		double[][] nei = new double[neighbors][dimensions];
		while(currentT <= maxcycle && repeat <= Maxkk){
			++ currentT;
			double best = f(current);
			int bestO = 0;
			for(int i = 0; i < neighbors; ++ i)
			{
				nei[i] = genFirst(i);
				if(f(nei[i]) < best)  {
					bestO = i;
					best = f(nei[i]);
				}
			}
			if(f(current) > best) {
				addIntoTB(current);
				copy(nei[bestO], current);
			}
			else ++ repeat;
			show();
		}
	}
	double dealDevi(double devi, double tar, double src)
	{
		Random rand = new Random();
		
		double factor = 1;
		if(devi < min){
			min  = devi;
			factor = 1;
		}
		else factor = Math.abs(devi) / Math.abs(min);
		if(devi > max) {
			max = devi;
			factor = 1;
		}
		else factor = Math.abs(devi) / Math.abs(max);
		
		if(Math.abs(devi) > radius) devi = radius;
		tar = src + 6 * devi * rand.nextDouble() - devi * 3;
		if(tar < rangeL){
			tar = rangeL + 1 / factor * radius; 
		}
		if(tar > rangeR){
			tar = rangeR - 1 / factor * radius;
		}
		return tar;
	}
	/**
	 * ����滻��ǰ���ith��Ԫ��
	 * @param ith
	 * @return
	 */
	double[] genFirst(int ith){
		double[] nei = new double[dimensions];
		double[] std = new double[dimensions];
		boolean flag = false;
		ith = ith % dimensions;
		
		copy(current, std);
		while (flag == false) {
			copy(current, nei);
			for (int i = 0; i <= ith; ++i) {
				std[i] = std[i] + step;
				double devi = f(std) - f(current);
				std[i] = current[i];
				nei[i] = dealDevi(devi, nei[i], current[i]);
			}
			if(inTB(nei) == false) flag = true;
		}
		return nei;
	}
	void secondStageSearch(){
		int repeat = 0;
		double[][] nei = new double[neighbors][dimensions];
		
		while(currentT <= maxcycle && repeat <= Maxkk){
			++ currentT;
			double best = f(current);
			boolean flag = false;
			
			for(int i = 0; i < neighbors; ++ i){
				copy(current, nei[i]);
				for (int j = 0; j < dimensions; ++j) {
					double temp = nei[i][j];
					nei[i][j] += step;
					double devi = f(nei[i]) - f(current);
					nei[i][j] = dealDevi(devi, nei[i][j], current[j]);
					
					if(inTB(nei[i]) == false && f(nei[i]) < best){
						best = f(nei[i]);
						addIntoTB(current);
						copy(nei[i], current);
						flag = true;
					}
					else nei[i][j] = temp;
				}
			}
		   if(flag == false) {
			   ++ repeat;
			   radius = radius * pp;
		   }
		   show();
		}
	}
	
	void thirdStageSearch(int n2){
		int repeat = 0;
		double[][] nei = new double[neighbors][dimensions];
		
		while(currentT <= maxcycle && repeat <= Maxkk){
			++ currentT;
			double best = f(current);
			boolean flag = false;
			
			for(int i = 0; i < neighbors; ++ i){
				copy(current, nei[i]);
				for (int j = 0; j < dimensions; ++j) {
					double temp = nei[i][j];
					nei[i][j] += step;
					double devi = f(nei[i]) - f(current);
					nei[i][j] = dealDevi(devi, nei[i][j], current[j]);
					
					if(inTB(nei[i]) == false && f(nei[i]) < best){
						best = f(nei[i]);
						addIntoTB(current);
						copy(nei[i], current);
						flag = true;
					}
					else nei[i][j] = temp;
				}
			}
		    if(flag == false) {
		    	++ repeat;
		    	step = step * por;
		    }
			show();
		}
	}
	void solve()
	{
		initialization();
		show();
		
		//��һ�׶�����
		System.out.println("############################��һ�׶�����$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
		neighbors = 5 * dimensions;
		radius = range;
		step = range;
		firstStageSearch();
		
		//�ڶ��׶�����
		System.out.println("############################�ڶ��׶�����$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
		neighbors = 5 * dimensions;
		secondStageSearch();
		
		//�����׶�����
		System.out.println("###############�����׶�����$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
		int n2 = neighbors;
		neighbors = 5 * dimensions;
		tabuList = new Tabu();
		thirdStageSearch(n2);
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
	public static void main(String[] args)
	{
		long start = System.currentTimeMillis();
		/*
		 * 1--30  	2--120 	3--200 	4--2 5--6  6 --60 7--2            8--2 MS        9-- 2  branin 10 --20 Brown3
		 *Ŀǰ��1��7��5��û��˿�����⣻3��������ֲ����ţ����ǿ�������ý����2��6�㲻���ý��
		 */
		MCTS cts = new MCTS(1);
		cts.solve();
		
		long end = System.currentTimeMillis();
		System.out.println();
		System.out.println(end -start);
	}
}
