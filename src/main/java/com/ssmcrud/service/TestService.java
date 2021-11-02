package com.ssmcrud.service;



import ai.onnxruntime.*;
import com.ssmcrud.bean.Payment;
import com.ssmcrud.dao.PaymentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lc
 * @create 2021-10-29 21:35
 */
@Service
public class TestService {

    @Autowired
    PaymentMapper paymentMapper;

    /**
     * 查询所有员工
     * @return
     */
    public List<Payment> getAll() {
        // TODO Auto-generated method stub
        return paymentMapper.selectByExample(null);
    }


    public void testModel( float[][][][] imgsFloat) throws OrtException {
//        Utilities.LoadTensorData();
        String modelPath = "D:\\DeskTop\\java-ssm\\ssmcrud-master\\src\\main\\resources\\yolox_s.onnx";
        OrtEnvironment env = OrtEnvironment.getEnvironment();
        OrtSession.SessionOptions opts = new OrtSession.SessionOptions();


        try (OrtSession session = env.createSession(modelPath, opts)) {
            Map<String, NodeInfo> inputMetaMap = session.getInputInfo();
            Map<String, OnnxTensor> container = new HashMap<>();
            NodeInfo inputMeta = inputMetaMap.values().iterator().next();
            // 输入的图片
//            float[] inputData = Utilities.ImageData[imageIndex];
            float[][][][] inputData = imgsFloat;
            String label = "haha";
            System.out.println("Selected image is the number: " + label);

            // this is the data for only one input tensor for this model
            Object tensorData = inputData;
//                    OrtUtil.reshape(inputData, ((TensorInfo) inputMeta.getInfo()).getShape());
            OnnxTensor inputTensor = OnnxTensor.createTensor(env, tensorData);
            container.put(inputMeta.getName(), inputTensor);

            // Run code omitted for brevity.

//            try (OrtSession session = env.createSession(modelPath, opts)) {


                // Load code not shown for brevity.

                // Run the inference
            try (OrtSession.Result results = session.run(container)) {

                // Only iterates once
                for (Map.Entry<String, OnnxValue> r : results) {
                    OnnxValue resultValue = r.getValue();
                    OnnxTensor resultTensor = (OnnxTensor) resultValue;
                    resultTensor.getValue();

//                    System.out.println("Output Name: {0}", r.Name);
//                    int prediction = MaxProbability(resultTensor);
//                    System.out.println("Prediction: " + prediction.ToString());
                }
            }


        }
    }

}
