# Hadoop MapReduce example

This project has been developed for the [ETSI Informáticos](https://fi.upm.es/) 2019/2020 **Data Warehouse** course during the **assignment E3** held by [Universidad Politécnica de Madrid](https://www.upm.es/) as a part of the Computer Science Degree.

Tutor: [Santiago Eibe García](http://www.upm.es/observatorio/vi/index.jsp?pageac=investigador.jsp&idInvestigador=8095)

## Table of contents
- [Introduction](#introduction)
- [Project Structure](#project-structure)
- [Dataset](#dataset)
- [MapReduce](#mapreduce)
---

### Introduction

This project tries to provide a simple example of Hadoop MapReduce paradigm.

The source data will be a medium-sized CSV file that simulates the output of a Shopping Center sales application.

The **purpose** of this Shopping Center is to know **the hour when the maximum number of sales occurs for each day, department, and product, named 'peak hour' in this project context**, so they can maximize their offer strategy.

### Project structure

This project tries to provide an example of Hadoop MapReduce paradigm. It involves the following applications:

- [datasetGenerator](https://github.com/neodmy/dwh-e3/tree/master/datasetGenerator): a simple JavaScript program to generate a dataset in csv format. By using [faker](https://www.npmjs.com/package/faker) and [node stream API](https://nodejs.org/api/stream.html), we have generated a simple dataset compose of 750,000 elements. More details about dataset elements [here](#dataset).

- [peekHour](https://github.com/Rodriblue/dwh/tree/master/hadoop-map-reduce/peekHour): a Java program that implements Hadoop MapReduce paradigm. It will be cover in more detail in [MapReduce](#mapreduce) section.


### Dataset

Each element in the dataset is composed of:

- Timestamp in ISO 8601 format
- Department name
- Product name

As example:

```csv
2018-12-21T19:04:53.779Z,Movies,Unbranded_Soft_Fish
```

You can find a 750,000 elements dataset [here](https://github.com/Rodriblue/dwh/tree/master/hadoop-map-reduce/datasetGenerator/dataset.csv) or generate a new one with:

```
node hadoop-map-reduce/datasetGenerator/index.js 
```

⚠️ **Don't forget to install dependency libraries before running the generator** with `npm install`

### MapReduce

##### Mapper

Say we have this element from the CSV file:

```csv
2018-12-21T19:04:53.779Z,Movies,Unbranded_Soft_Fish
```

We first parse the element to extract the three fields mentioned before (timestamp, department and product).

Then, we parse the timestamp to get these two values:

- Date with format `yyyy/MM/dd` (date1)
- Hour with format `HH` (date2)

So given the example, we get `2018/21/21` (date1) and `19` (date2).

With date1, department and product we generate a composed **key** `date1-department-product`. For this particular example, this key would be `2018/21/21-Movies-Unbranded_Soft_Fish`

As of the **value**, we set it to be date2, in this case `19`.

The result of this Mapper would be `2018/21/21-Movies-Unbranded_Soft_Fish    19`

##### Reducer

The input that the Reducer will receive given the Mapper will be the grouped keys with a list of hours similar to:

`2018/21/21-Movies-Unbranded_Soft_Fish    [19, 12, 15, 12, 16, 19]`

As the goal is to know when the peek hour occurs, we evaluate the list of occurrence of each hour and then calculate the maximum. As a result, for the previous example we get:

`2018/21/21-Movies-Unbranded_Soft_Fish    12`

To simplify the problem, if more than one hour gets the same peek value (12 and 19 in the example), we stick to the first one.
