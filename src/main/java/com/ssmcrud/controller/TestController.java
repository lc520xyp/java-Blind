package com.ssmcrud.controller;

import ai.onnxruntime.OrtException;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ssmcrud.bean.Msg;
import com.ssmcrud.bean.Payment;
import com.ssmcrud.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import sun.misc.BASE64Decoder;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

/**
 * @author lc
 * @create 2021-10-29 21:34
 */
@Controller
public class TestController {

    @Autowired
    TestService testService;

    static BASE64Decoder decoder = new sun.misc.BASE64Decoder();


    @RequestMapping("/payments")
    @ResponseBody
    public Msg getEmpsWithJson(
            @RequestParam(value = "pn", defaultValue = "1") Integer pn) {
        // 这不是一个分页查询
        // 引入PageHelper分页插件
        // 在查询之前只需要调用，传入页码，以及每页的大小
        PageHelper.startPage(pn, 5);
        // startPage后面紧跟的这个查询就是一个分页查询
        List<Payment> payments = testService.getAll();
        // 使用pageInfo包装查询后的结果，只需要将pageInfo交给页面就行了。
        // 封装了详细的分页信息,包括有我们查询出来的数据，传入连续显示的页数
        PageInfo page = new PageInfo(payments, 5);
        return Msg.success().add("pageInfo", page);
    }
    @RequestMapping("/send/picture")
    @ResponseBody
    public Map<String, Object > getPicture(
//            @RequestPart("file") MultipartFile files) {
            @RequestParam String base64Data) throws IOException, OrtException {
        System.out.println(base64Data);
        base64Data = base64Data.split("base64,")[1];

        byte[] bytes1 = decoder.decodeBuffer(base64Data);

        System.out.println(bytes1.length);

        ByteArrayInputStream bais = new ByteArrayInputStream(bytes1);
        BufferedImage bi1 = ImageIO.read(bais);

        int width = bi1.getWidth();
        int height = bi1.getHeight();


        System.out.println(width + " " + height);

        float[][][][] imgsFloat= new float[1][3][640][640];

        for(int i=bi1.getMinX();i<width;i++) {
            for(int j=bi1.getMinY();j<height;j++)
            {
                int pixel=bi1.getRGB(i,j);
                imgsFloat[0][0][i][j] = (float)((pixel &  0xff0000) >> 16);
                imgsFloat[0][1][i][j] = (float)((pixel &  0xff00) >> 8);
                imgsFloat[0][2][i][j] = (float)(pixel &  0xff);
//                System.out.println("i="+i+",j="+j+":("+rgb[0]+","+rgb[1]+","+rgb[2]+")");
            }
        }

        testService.testModel(imgsFloat);




        Map<String,Object> map = new HashMap<String, Object>();
        map.put("result", "success");
        return map;
    }
}
