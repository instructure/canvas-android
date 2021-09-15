const jsdom = require('jsdom')
const { JSDOM } = jsdom
const afterLoad = require('after-load');

afterLoad('./apps/student/build/reports/jacoco/combinedJacoco/html/index.html', function (html) {
    const dom = new JSDOM(html);
    console.log(dom.window.document.querySelector('tfoot').querySelector('tr').querySelectorAll('td')[2].textContent);
});