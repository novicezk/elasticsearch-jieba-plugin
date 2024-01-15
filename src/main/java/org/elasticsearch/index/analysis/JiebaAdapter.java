package org.elasticsearch.index.analysis;

import java.io.IOException;
import java.io.Reader;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.JiebaSegmenter.SegMode;
import com.huaban.analysis.jieba.SegToken;

public class JiebaAdapter implements Iterator<SegToken> {

  private final static JiebaSegmenter jiebaTagger = new JiebaSegmenter();

  private final SegMode segMode;

  private Iterator<SegToken> tokens;

  private String raw = null;

  public JiebaAdapter(String segModeName) {


    System.out.println("init jieba adapter");
    if (null == segModeName) {
      segMode = SegMode.SEARCH;
    } else {
      segMode = SegMode.valueOf(segModeName);
    }
  }

  public synchronized void reset(Reader input) {
    try {
      StringBuilder bdr = new StringBuilder();
      char[] buf = new char[1024];
      int size = 0;
      while ((size = input.read(buf, 0, buf.length)) != -1) {
        String tempstr = new String(buf, 0, size);
        bdr.append(tempstr);
      }
      raw = bdr.toString().trim();
    } catch (IOException e) {
      e.printStackTrace();
    }

    List<SegToken> list = jiebaTagger.process(raw, segMode);
    list.sort((o1, o2) -> {
      if (o1.startOffset < o2.startOffset) {
        return -1;
      }
      if (o1.startOffset > o2.startOffset) {
        return 1;
      }
      if (o1.startOffset == o2.startOffset) {
        if (o1.endOffset < o2.endOffset) {
          return -1;
        }
        return 1;
      }
      return 0;
    });
    tokens = list.iterator();
  }

  @Override
  public boolean hasNext() {
    return tokens.hasNext();
  }

  @Override
  public SegToken next() {
    return tokens.next();
  }

  @Override
  public void remove() {
    tokens.remove();
  }
}