/* eslint-disable no-plusplus */
/* eslint-disable class-methods-use-this */
/* eslint-disable no-underscore-dangle */
const faker = require('faker');
const fs = require('fs');
const path = require('path');
const { Readable } = require('stream');

class ProductGenerator extends Readable {
  constructor(total) {
    super();
    this._total = total;
    this._index = 1;
  }

  _generateProductSale() {
    const date = faker.date.between('2018/01/01', '2020/05/22');
    const category = faker.commerce.department();
    const product = faker.commerce.productName();
    return `${date.toISOString()},${category},${product.split(' ').join('_')}`;
  }

  _read() {
    const i = this._index++;
    if (i > this._total) this.push(null);
    else {
      const str = `${this._generateProductSale()}\n`;
      this.push(Buffer.from(str));
    }
  }
}

const productGenerator = new ProductGenerator(750000);


const writeStream = fs.createWriteStream(path.join(__dirname, 'dataset.csv'));

productGenerator.on('data', (chunk) => {
  writeStream.write(chunk);
});

productGenerator.on('end', () => {
  writeStream.close();
})
