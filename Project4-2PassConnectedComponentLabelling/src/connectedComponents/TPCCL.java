package connectedComponents;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.highgui.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.lang.Math;

public class TPCCL {

	private static List<TreeSet<Integer>> labelTree = new ArrayList<TreeSet<Integer>>();
	private static final int colorMap[] = {0x808000, 0x808080, 0xC0C0C0, 0x008000, 0x000080, 0x800080, 0xFF0000, 0x00FF00, 0x800000, 0xE6E6FA, 0xFFD700, 0x0000FF, 0xFF00FF, 0xFFFF00, 0x008080, 0x00FFFF};

	@SuppressWarnings("resource")
	public static void main(String[] args) throws FileNotFoundException {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		Mat binaryImage = Highgui.imread("/Users/adithiathreya/Desktop/SCU/COEN296-VideoProcessing/components.png");
		Mat binaryImageGray = new Mat();
		Imgproc.cvtColor(binaryImage, binaryImageGray , Imgproc.COLOR_RGB2GRAY);
		Mat label = Mat.zeros(binaryImageGray.rows(), binaryImageGray.cols(), CvType.CV_16UC1);
		double newLabel = 0;

		//1st Pass
		for(int i=0; i<binaryImageGray.rows(); ++i) {
			for(int j=0; j<binaryImageGray.cols(); ++j) {
				if ((binaryImageGray.get(i, j)[0] == 255) 
						&& (binaryImageGray.get(i, j)[0] == binaryImageGray.get(i, j-1)[0]) 
						&& (binaryImageGray.get(i, j)[0] == binaryImageGray.get(i-1, j)[0]) 
						&& (label.get(i-1, j)[0] != label.get(i, j-1)[0])) {
					label.put(i, j, Math.min(label.get(i-1, j)[0], label.get(i, j-1)[0]));

					List<TreeSet<Integer>> tempList = new ArrayList<TreeSet<Integer>>();
					TreeSet<Integer> foundTreeSet = new TreeSet<Integer>();
					Boolean found = false;
					for(TreeSet<Integer> tempTreeSet: labelTree) {
						Boolean temp1 = tempTreeSet.contains((int) label.get(i-1, j)[0]);
						Boolean temp2 = tempTreeSet.contains((int) label.get(i, j-1)[0]);

						if(temp1 || temp2) {
							if(found) {
								tempList.add(tempTreeSet);
								continue;
							}
							found = true;
							foundTreeSet = tempTreeSet;
							if (temp1 && temp2) {
								break;
							} else if(temp1) {
								tempTreeSet.add((int) label.get(i, j-1)[0]);

							} else {
								tempTreeSet.add((int) label.get(i-1, j)[0]);
							}
						} 
					}
					if(!found) {
						TreeSet<Integer> newTreeSet = new TreeSet<Integer>();
						newTreeSet.add((int) label.get(i, j-1)[0]);
						newTreeSet.add((int) label.get(i-1, j)[0]);
						labelTree.add(newTreeSet);	
					} else {
						for(TreeSet<Integer> tempTreeSet: tempList) {
							foundTreeSet = unionTreeSets(foundTreeSet, tempTreeSet);
							labelTree.remove(tempTreeSet);
						}
					}
				} else if ((binaryImageGray.get(i, j)[0] == 255) 
						&& (binaryImageGray.get(i, j)[0] == binaryImageGray.get(i, j-1)[0])) {
					label.put(i, j, label.get(i, j-1)[0]);
				} else if ((binaryImageGray.get(i, j)[0] == 255) 
						&& (binaryImageGray.get(i, j)[0] == binaryImageGray.get(i-1, j)[0])) {
					label.put(i, j, label.get(i-1, j)[0]);
				} else if (binaryImageGray.get(i, j)[0] == 255) {
					label.put(i, j, ++newLabel);
				} else {
					continue;
				}
			}
		}
		PrintStream ps = new PrintStream(new FileOutputStream(new File("label1stPass.txt")));
		ps.println(label.dump());
		System.out.println("labelTreeSize : " + labelTree.size());
		for(TreeSet<Integer> tempTreeSet: labelTree) {
			System.out.println(tempTreeSet.toString());
		}


		//2nd Pass
		Mat colorLabel = Mat.zeros(binaryImageGray.rows(), binaryImageGray.cols(), CvType.CV_8UC3);
		for(int i=0; i<binaryImageGray.rows(); ++i) {
			for(int j=0; j<binaryImageGray.cols(); ++j) {
				if((int) label.get(i, j)[0] != 0) {
					for(TreeSet<Integer> tempTreeSet: labelTree) {
						if(tempTreeSet.contains((int) label.get(i, j)[0])) {
							int tempColor = colorMap[labelTree.indexOf(tempTreeSet)];
							double x[] = new double[3];
							x[2] = (tempColor>>16)&0xFF;
							x[1] = (tempColor>>8)&0xFF;
							x[0] = (tempColor)&0xFF;
							colorLabel.put(i, j, x);
						}
					}
				}
			}
		}

		Highgui.imwrite("label2.png",colorLabel);
	}

	private static TreeSet<Integer> unionTreeSets(TreeSet<Integer> tmpTrSt1, TreeSet<Integer> tmpTrSt2) {
		Iterator<Integer> iter = tmpTrSt2.iterator();
		while(iter.hasNext()) {
			Integer currentLabel = iter.next();
			if(!tmpTrSt1.contains(currentLabel)) {
				tmpTrSt1.add(currentLabel);
			}
		}
		return tmpTrSt1;
	}
}
