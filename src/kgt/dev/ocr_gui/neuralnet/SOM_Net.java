package kgt.dev.ocr_gui.neuralnet;

import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.ml.data.basic.BasicMLDataPair;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.neural.som.SOM;
import org.encog.neural.som.training.clustercopy.SOMClusterCopyTraining;

import kgt.dev.ocr_gui.model.TrainingSet;
import kgt.dev.ocr_gui.neuralnet.createSets.SampleData;

public class SOM_Net extends NeuralNets{

	private static final long serialVersionUID = 7966537469301647951L;

	private SOM network;
	
	private String message;
	
	/**
	 * CONSTRUCTOR
	 * 
	 * Used for automated creation of the SOM network 
	 * based on the training set
	 * 
	 * @param newTs - training set to add to the self organizing map
	 */
	public SOM_Net(TrainingSet newTs){
		super(newTs);
		this.setTrained(false);
	}
	
	/**
	 * CONSTRUCTOR
	 * 
	 * Used for manual creation of the SOM
	 * 
	 * @param sample_width
	 * @param sample_height
	 */
	public SOM_Net(){
		this.setTrained(false);
	}
	
	/**
	 * Set the sample width and height to establish input and output
	 * neuron count.
	 */
	public void setSampleDim(){
		
		if(this.trainSet.getTrainingSet().isEmpty()){
			throw new IndexOutOfBoundsException();
		}else{
			this.SET_SIZE = this.trainSet.getTrainingSet().size();
			this.SAMPLE_WIDTH = this.trainSet.getTrainingSet().get(0).getWidth();
			this.SAMPLE_HEIGHT = this.trainSet.getTrainingSet().get(0).getHeight();
		}
	}
	
	@Override
	public void train(){
		
		try{
			int inputNeurons = this.SAMPLE_WIDTH * this.SAMPLE_HEIGHT;
			int outputNeurons = this.SET_SIZE;
			
			final MLDataSet dataSet = new BasicMLDataSet();
			
			for(int e = 0; e < this.SET_SIZE;e++){
				
				final MLData data = new BasicMLData(inputNeurons);
				
				int index =0;
				final SampleData ds = this.trainSet.getTrainingSet().get(e);
				
				for(int x = 0; x < ds.getWidth();x++){
					for(int y = 0; y < ds.getHeight();y++){
						
						if(ds.getBiPolarData(x,y)){
							data.setData(index++, 1.0);
						}else{
							data.setData(index++, -1.0);
						}
					}
				}
				dataSet.add(new BasicMLDataPair(data,null));
			}
			
			this.network = new SOM(inputNeurons,outputNeurons);
			this.network.reset();
			
			SOMClusterCopyTraining train = new SOMClusterCopyTraining(this.network,dataSet);
			train.iteration();
			this.setTrained(true);
		}catch(Exception e){
			setMessage("Error Training the Self Orginising Map");
		}
	}
	
	/**
	 * Associates neuron to a character
	 * 
	 * @return - neuron to char map 
	 */
	public char[] mapNeurons(){
		final char neuronMap[] = new char[SET_SIZE];
		
		for(int n = 0;n< neuronMap.length;n++){
			neuronMap[n] = '*';
		}
		
		for(int i =0; i < SET_SIZE; i++){
			MLData DataIn = new BasicMLData(SAMPLE_WIDTH * SAMPLE_HEIGHT);
			
			int index = 0;
			SampleData ds = this.trainSet.getTrainingSet().get(i);
			
			for(int x = 0; x < ds.getWidth(); x++){
				for(int y = 0; y < ds.getHeight(); y++){
					
					if(ds.getBiPolarData(x, y)){
						DataIn.setData(index++, 1.0);
					}else{
						DataIn.setData(index++, -1.0);
					}
				}
				int winner = this.network.classify(DataIn);
				neuronMap[winner] = ds.getSymbol();
			}
		}
		return neuronMap;
	}
	
	/**
	 * Check which neuron is fired to determine the closest match.
	 * 
	 * @param test - the character in question
	 * @return - the winning character
	 */
	@Override
	public char recognise(SampleData test){
		char best = ' ';
		try{
			MLData input = new BasicMLData(SAMPLE_WIDTH * SAMPLE_HEIGHT);
			int index = 0;
			SampleData ds = test;
			
			for(int x = 0; x < ds.getWidth(); x++){
				for(int y = 0; y < ds.getHeight(); y++){
					
					if(ds.getBiPolarData(x, y)){
						input.setData(index++, 1.0);
					}else{
						input.setData(index++, -1.0);
					}
				}
			}
			
			int winner = this.network.classify(input);
			char map[] = mapNeurons();
			
			best = map[winner];
			
		}catch(NullPointerException non){
			this.setMessage("The network needs to be trained first!");
		}
		return best;
	}
	
	/**
	 * Adds a training set for manual creation of SOM
	 * 
	 * @param newTs - the SampleData set to train with
	 */
	public void addTrainingSet(TrainingSet newTs){
		
		this.trainSet = newTs;
		if(newTs.getTrainingSet().isEmpty()){
			this.setMessage("Error, there is no data in the training set!");
			throw new NullPointerException();
		}else{
			this.setTrained(false);
			this.SET_SIZE = newTs.getTrainingSet().size();
		}
	}
	
	/**
	 * Set alert messages for SOM class
	 * 
	 * @param newMessage
	 */
	@Override
	protected void setMessage(String newMessage){
		this.message = newMessage;
	}
	
	/**
	 * @return - get the current alert message
	 */
	@Override
	public String getMessage(){
		return message;
	}
}
