package com.ssmcrud.util;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class YOLOX {
    private OrtSession session;
    private OrtEnvironment env;
    private float ratio;

    public YOLOX(String modelfile)  {

        try {
            this.initSession(modelfile);
        } catch (OrtException e) {
            e.printStackTrace();
        }
    }

    public void initSession(String modelfile) throws OrtException {
        if (modelfile == null) {
            System.out.println("Usage: <model-path>");
            return;
        }

        this.env = OrtEnvironment.getEnvironment();
        OrtSession.SessionOptions opts = new OrtSession.SessionOptions();
        //opts.addCUDA(0);
        System.out.println("Loading model from " + modelfile);
        this.session = this.env.createSession(modelfile, opts);
    }

    public BufferedImage preprocess(BufferedImage bufIma, int[] input_size){
        int width=bufIma.getWidth();
        int height=bufIma.getHeight();

        this.ratio = Math.min((float)input_size[0] / (float)width, (float)input_size[1] / (float)height);

        BufferedImage newImage = new BufferedImage(input_size[0], input_size[1], BufferedImage.TYPE_INT_BGR);
        Graphics graphics = newImage.createGraphics();
        graphics.setColor(new Color(114, 114, 114));
        graphics.fillRect(0, 0, input_size[0], input_size[1]);
        graphics.drawImage(bufIma, 0, 0, (int)(width*ratio), (int)(height*ratio), null);

        return newImage;
    }

    public float[][][][] img2RGB(BufferedImage img){
        int width=img.getWidth();
        int height=img.getHeight();
        float[][][][] imaRGB = new float[1][3][height][width];

        for(int i=0;i<height;i++)
            for(int j=0;j<width;j++){
                int pix = img.getRGB(j,i);
                imaRGB[0][2][i][j] = (pix & 0xff0000) >> 16;
                imaRGB[0][1][i][j] = (pix & 0xff00) >> 8;
                imaRGB[0][0][i][j] = (pix & 0xff);
            }
        return imaRGB;
    }

    public float[][][] demo_postprocess(float[][][] output, int[] img_size){
        int[] strides = {8, 16, 32};
        int[] hsizes = new int[3];
        int[] wsizes = new int[3];
        for(int i=0;i<strides.length;i++){
            hsizes[i] = img_size[0] / strides[i];
            wsizes[i] = img_size[1] / strides[i];
        }

        int endcount = wsizes[0]*hsizes[0]+wsizes[1]*hsizes[1]+wsizes[2]*hsizes[2];
        int count = 0;
        int[][][] grids= new int[1][endcount][2];
        int[][][] expanded_strides= new int[1][endcount][1];

        for(int i=0;i<strides.length;i++){
            int[][] xv = new int[wsizes[i]][wsizes[i]];
            int[][] yv = new int[hsizes[i]][hsizes[i]];
            for(int j=0;j<wsizes[i];j++){
                for(int k=0;k<wsizes[i];k++){
                    xv[k][j]=j;
                    yv[j][k]=j;
                }
            }
            for(int j=0;j<wsizes[i];j++){
                for(int k=0;k<wsizes[i];k++){
                    grids[0][count][0] = xv[j][k];
                    grids[0][count][1] = yv[j][k];
                    expanded_strides[0][count][0] = strides[i];
                    count++;
                }
            }
        }

        for(int i=0;i<endcount;i++){
            output[0][i][0] = (output[0][i][0] + grids[0][i][0]) * expanded_strides[0][i][0];
            output[0][i][1] = (output[0][i][1] + grids[0][i][1]) * expanded_strides[0][i][0];
            output[0][i][2] = (float) (Math.exp(output[0][i][2]) * expanded_strides[0][i][0]);
            output[0][i][3] = (float) (Math.exp(output[0][i][3]) * expanded_strides[0][i][0]);
        }

        return output;
    }

    public ArrayList<float[]> nms_cls(float[][] boxes, float[][] scores, float nms_thr, float score_thr){
        ArrayList<float[]> final_boxes = new ArrayList<>();
        int length = scores.length;
        int num_classes = scores[0].length;
        for(int i=0;i<num_classes;i++){
            ArrayList<float[]> boxes_cls = new ArrayList<>();
            for(int j=0;j<length;j++){
                if(scores[j][i]>score_thr){
                    float x1 = boxes[j][0];
                    float y1 = boxes[j][1];
                    float x2 = boxes[j][2];
                    float y2 = boxes[j][3];
                    float areas = (x2 - x1 + 1) * (y2 - y1 + 1);
                    float[] candidate = {scores[j][i], areas, x1, y1, x2, y2};
                    boxes_cls.add(candidate);
                }
            }
            boxes_cls.sort(new Comparator<float[]>() {
                @Override
                public int compare(float[] o1, float[] o2) {
                    return Float.compare(o1[0], o2[0]);
                }
            });
            while (!boxes_cls.isEmpty()){
                ArrayList<float[]> boxes_tem = new ArrayList<>();
                float[] best_box = boxes_cls.get(boxes_cls.size()-1);
                boxes_cls.remove(boxes_cls.size()-1);
                for (float[] boxes_cl : boxes_cls) {
                    float xx1 = Math.max(best_box[2], boxes_cl[2]);
                    float yy1 = Math.max(best_box[3], boxes_cl[3]);
                    float xx2 = Math.min(best_box[4], boxes_cl[4]);
                    float yy2 = Math.min(best_box[5], boxes_cl[5]);
                    float inter = (float) (Math.max(0.0, xx2 - xx1 + 1) * Math.max(0.0, yy2 - yy1 + 1));
                    float ovr = inter / (best_box[1] + boxes_cl[1] - inter);
                    if (ovr <= nms_thr) boxes_tem.add(boxes_cl);
                }
                best_box[1] = (float) i;
                final_boxes.add(best_box);
                boxes_cls = boxes_tem;
            }
        }

        return final_boxes;
    }

    public ArrayList<float[]> nms(float[][] boxes, float[][] scores, float nms_thr, float score_thr){
        ArrayList<float[]> final_boxes = new ArrayList<>();
        int length = scores.length;
        float[][] scores_n = new float[length][2];
        for(int i=0;i<scores.length;i++){
            float max_cls = -1;
            float max_score = 0.0f;
            for(int j=0;j<scores[i].length;j++){
                if(scores[i][j] > max_score){
                    max_score = scores[i][j];
                    max_cls = j;
                }
            }
            scores_n[i][0] = max_score;
            scores_n[i][1] = max_cls;
        }
        ArrayList<float[]> boxes_cls = new ArrayList<>();
        for(int i=0;i<length;i++){
            if(scores_n[i][0]>score_thr){
                float x1 = boxes[i][0];
                float y1 = boxes[i][1];
                float x2 = boxes[i][2];
                float y2 = boxes[i][3];
                float areas = (x2 - x1 + 1) * (y2 - y1 + 1);
                float[] candidate = {scores_n[i][0], areas, x1, y1, x2, y2, scores_n[i][1]};
                boxes_cls.add(candidate);
            }
        }
        boxes_cls.sort(new Comparator<float[]>() {
            @Override
            public int compare(float[] o1, float[] o2) {
                return Float.compare(o1[0], o2[0]);
            }
        });
        while (!boxes_cls.isEmpty()){
            ArrayList<float[]> boxes_tem = new ArrayList<>();
            float[] best_box = boxes_cls.get(boxes_cls.size()-1);
            boxes_cls.remove(boxes_cls.size()-1);
            for (float[] boxes_cl : boxes_cls) {
                float xx1 = Math.max(best_box[2], boxes_cl[2]);
                float yy1 = Math.max(best_box[3], boxes_cl[3]);
                float xx2 = Math.min(best_box[4], boxes_cl[4]);
                float yy2 = Math.min(best_box[5], boxes_cl[5]);
                float inter = (float) (Math.max(0.0, xx2 - xx1 + 1) * Math.max(0.0, yy2 - yy1 + 1));
                float ovr = inter / (best_box[1] + boxes_cl[1] - inter);
                if (ovr <= nms_thr) boxes_tem.add(boxes_cl);
            }
            best_box[1] = best_box[6];
            float[] best_box_ind = Arrays.copyOf(best_box,6);
            final_boxes.add(best_box_ind);
            boxes_cls = boxes_tem;
        }
        return final_boxes;
    }

    public ArrayList<float[]> pred(float[][] predictions, float nms_thr, float score_thr, boolean class_agnostic) {
        int length = predictions.length;
        int classnum = predictions[0].length-4-1;

        //float[][] boxes = new float[length][4];
        float[][] scores = new float[length][classnum];
        float[][] boxes_xyxy = new float[length][4];
        for(int i=0;i<length;i++){
            boxes_xyxy[i][0] = (predictions[i][0] - predictions[i][2]/2)/this.ratio;
            boxes_xyxy[i][1] = (predictions[i][1] - predictions[i][3]/2)/this.ratio;
            boxes_xyxy[i][2] = (predictions[i][0] + predictions[i][2]/2)/this.ratio;
            boxes_xyxy[i][3] = (predictions[i][1] + predictions[i][3]/2)/this.ratio;
            for(int j=0;j<classnum;j++){
                scores[i][j] = predictions[i][4] * predictions[i][5+j];
            }
        }
        if(class_agnostic)
            return nms(boxes_xyxy, scores, nms_thr, score_thr);
        else
            return nms_cls(boxes_xyxy, scores, nms_thr, score_thr);
    }

    public float[][][] forward(float[][][][] testData) throws OrtException {
        float[][][] output = new float[1][8400][85];
        if (testData.length != 1 ||
                testData[0].length != 3 ||
                testData[0][0].length != 640 ||
                testData[0][0][0].length != 640) {
            System.out.println("Inputsize: 1,3,640,640");
            return output;
        }
        //将testData转为OnnxTensor
        OnnxTensor test = OnnxTensor.createTensor(this.env, testData);
        String inputName = this.session.getInputNames().iterator().next();
        OrtSession.Result res = this.session.run(Collections.singletonMap(inputName, test));
        output = (float[][][]) res.get(0).getValue();
        return output;
    }

    public ArrayList<float[]> run(BufferedImage bufIma, float score_threshold) throws OrtException {
        long endTime=System.currentTimeMillis();
        long startTime=System.currentTimeMillis();
        int[] inputSize = {640,640};

        BufferedImage resizeIma = this.preprocess(bufIma, inputSize);

//        endTime=System.currentTimeMillis();
//        System.out.println("程序运行时间： "+(endTime-startTime)+"ms");
//        startTime=System.currentTimeMillis();

        float[][][][] imaRGB = this.img2RGB(resizeIma);

//        endTime=System.currentTimeMillis();
//        System.out.println("程序运行时间： "+(endTime-startTime)+"ms");
//        startTime=System.currentTimeMillis();

        float[][][] output = this.forward(imaRGB);

//        endTime=System.currentTimeMillis();
//        System.out.println("程序运行时间： "+(endTime-startTime)+"ms");
//        startTime=System.currentTimeMillis();

        float[][] predictions = this.demo_postprocess(output, inputSize)[0];

//        endTime=System.currentTimeMillis();
//        System.out.println("程序运行时间： "+(endTime-startTime)+"ms");
//        startTime=System.currentTimeMillis();

        return this.pred(predictions, 0.45f, score_threshold, true);
    }
}
