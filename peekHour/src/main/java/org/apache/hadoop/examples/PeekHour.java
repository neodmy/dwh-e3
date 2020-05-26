package org.apache.hadoop.examples;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.Date;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class PeekHour {

  public static class TokenizerMapper extends Mapper<Object, Text, Text, IntWritable> {

    private Text mapKey = new Text();
    private IntWritable mapValue = new IntWritable();
    private DateTimeFormatter inputFormatter = DateTimeFormatter.ISO_DATE_TIME;
    private SimpleDateFormat keyFormatter = new SimpleDateFormat("yyyy/MM/dd");
    private SimpleDateFormat valueFormatter = new SimpleDateFormat("HH");

    public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
      StringTokenizer itr = new StringTokenizer(value.toString());

      while (itr.hasMoreTokens()) {
        String[] entryValues = itr.nextToken().split(",");
        System.out.println(Arrays.toString(entryValues));

        TemporalAccessor accesor = inputFormatter.parse(entryValues[0]);
        Date date = Date.from(Instant.from(accesor));

        String formattedKeyDate = keyFormatter.format(date);
        int fromattedValueDate = Integer.parseInt(valueFormatter.format(date));
        
        String customKey = String.join("-", formattedKeyDate, entryValues[1], entryValues[2]);

        mapKey.set(customKey);
        mapValue.set(fromattedValueDate);
        context.write(mapKey, mapValue);
      }
    }
  }

  public static class FrequencyHourReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
    private IntWritable result = new IntWritable();

    public void reduce(Text key, Iterable<IntWritable> values, Context context)
        throws IOException, InterruptedException {

      int[] hours = new int[24];
      for (IntWritable val : values) {
        int hour = val.get();
        hours[hour]++;
      }

      int index = 0;
      int max = hours[0];
      for (int i = 1; i < 24; i++) {
        int current = hours[i];
        if (current > max) {
          index = i;
          max = current;
        }
      }
      result.set(index);
      context.write(key, result);
    }
  }

  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    Job job = Job.getInstance(conf, "Peek Hour");
    job.setJarByClass(PeekHour.class);
    job.setMapperClass(TokenizerMapper.class);
    job.setCombinerClass(FrequencyHourReducer.class);
    job.setReducerClass(FrequencyHourReducer.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(IntWritable.class);
    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}