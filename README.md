# UK Prime Minister Speech Scraper

A simple automation to create dataset consisting of UK Prime Minister speeches.
Speeches are collected from the official site: GOV.UK

The dataset made through this automation is just a small portion of the entire dataset we are working on for my (and my team's) undergraduate thesis research.
Our thesis title is: A sentimental Analysis on Political Speeches

The speech collection is automated with the help of a third party HTML parsing java library called JSOUP.
Runtime of the entire process is optimized through multithreading.

Finally, the collected speeches are exported to a csv file with the help of library OpenCSV.
