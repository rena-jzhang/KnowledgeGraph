/** 
* @Title: BaiduBaikeSpider.java
* @Package: edu.heu.kg.spider 
* @date:
*/
package edu.heu.kg.spider;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.ConsolePipeline;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Html;
 

public class BaiduBaikeSpider implements PageProcessor {

    // 抓取配置
    private Site site = Site.me().setRetryTimes(3).setSleepTime(500).setCharset("UTF-8").setUserAgent(
    		"Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:56.0) Gecko/20100101 Firefox/56.0");
	
    // 检索词
    private static String word;
    // 存储路经
    private static final String PATH = "D:\\";
     
    @Override
    public Site getSite() {
        return site;
    }

    @Override
    public void process(Page page) { 
    	List<String> list; 
    	if (!page.getUrl().regex("https://baike.baidu.com/item/.*").match()) {
        
    		list = page.getHtml().xpath("//*[@id=\"body_wrapper\"]/div[1]/dl/dd/a/@href").all();
        
    		try {
    			list=  UrlDecode(list );
    			System.out.println(list);
    		} catch (UnsupportedEncodingException e1) {
    			// TODO 自动生成的 catch 块
    			e1.printStackTrace();
    		}
    		for(int i=0; i<list.size()&&i<5;++i) {
    			String str=list.get(i); 
    			if(   true) { 
    				page.addTargetRequest("https://baike.baidu.com/item/"+str); 
//    				if(i>0&&i<2)
//    				page.addTargetRequest("https://baike.baidu.com/search?word=" +  str); 
    			}
    		}
        }
        if (page.getUrl().regex("https://baike.baidu.com/item/.*").match()) {

            Html html = page.getHtml();

            if (html.xpath("/html/body/div[3]/div[2]/div/div[2]/dl[1]/dd/h1/text()").match()) {

                // 标题
                String title = html.xpath("/html/body/div[3]/div[2]/div/div[2]/dl[1]/dd/h1/text()").toString();
                // 摘要
                String summary = html.xpath("/html/body/div[3]/div[2]/div/div[2]/div[@class='lemma-summary']/allText()")
                        .all().toString().replaceAll(",", "");
                 StringBuilder sb = new StringBuilder();
                 // 同义词
//                  String synonym = html.xpath("/html/body/div[3]/div[2]/div/div[2]/span/span/text()").all().toString();
                 System.out.println(title+'\n'+summary);
                sb.append(title + ","  + summary + "\n"   );
                try {
                    outPut(PATH + "百科词条.txt", sb.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else {
                page.addTargetRequest("https://baike.baidu.com/search/none?word=" + word);
            }
        }
    }

    // URL解码
    @SuppressWarnings("unused")
    private static List<String> UrlDecode(List<String> rawList) throws UnsupportedEncodingException {

        List<String> resultList = new LinkedList<>();
        String reg = "https://baike.baidu.com/item/(.*)/\\d+";
        Pattern p = Pattern.compile(reg);
        Matcher m;
        for (String str : rawList) {
            m = p.matcher(str);
            if (m.find()) {
                resultList.add(java.net.URLDecoder.decode(m.group(1), "UTF-8"));
            }
        }
        return resultList;

    }

    // 存储
    private static void outPut(String path, String content) throws IOException {
 
    	FileOutputStream fos = new FileOutputStream(path, true);
        OutputStreamWriter osw = new OutputStreamWriter(fos);
        BufferedWriter bw = new BufferedWriter(osw);
        bw.write("name"+","+"summary"+'\n'+content);
        bw.close();

    }

    public static void main(String[] args) throws IOException {

        try {
            BufferedReader br = new BufferedReader(new FileReader("D:\\名词.txt"));
            while ((word = br.readLine()) != null) {
                // 创建Spider，addUrl的参数可以为可变参数，但是会有问题
            	if(word.equals("")||word.equals("\n")) continue;
                Spider.create(new BaiduBaikeSpider()).addPipeline(new ConsolePipeline())
                        .addUrl("https://baike.baidu.com/search?word=" + word).run();
            }
            br.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

    }

}