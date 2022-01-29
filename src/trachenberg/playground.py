# %%
import sys
import os

sys.path.append(os.path.abspath(os.path.join('./src/trachenberg')))

# %%
import numpy as np
import pandas as pd
import seaborn as sns

# %%


def read_stock_data(symbol):
    df = pd.read_json('./resources/stock_data/{}.json'.format(symbol))
    df.index = df['date']
    df = df.resample('D').agg(
        {'close': 'last', 'open': 'first', 'high': 'max', 'low': 'min', 'volume': 'sum'})
    return df


def read_twttr_data(symbol):
    df = pd.read_json('./resources/twttr_data/{}.json'.format(symbol))
    df['date'] = pd.to_datetime(df['created-at'])
    df.index = df['date']
    df = df.sort_index()
    df = df.drop(['created-at', 'date', 'id'], axis=1)
    df['mscore'] = df['magnitude'] * df['score']
    df = df.resample('D').agg({'score': 'mean', 'mscore': ['mean', 'size']})
    df.columns = ['score', 'mscore', 'size']
    df = df.fillna(0)
    return df


def compare_twttr_stock(symbol):
    stock = read_stock_data(symbol)
    twttr = read_twttr_data(symbol)
    stock['score'] = twttr['score']
    stock['mscore'] = twttr['mscore']
    stock['ret'] = stock['close'].pct_change(1)
    stock = stock.dropna()
    stock = stock.loc[stock['score'] != 0]
    return stock[['ret', 'score', 'mscore', 'volume']].corr()

# %%
# forcompare_twttr_stock('aapl')['score']['ret']

result = {}
for file in os.listdir('./resources/twttr_data'):
    symbol = file.split('.')[0]
    corr = compare_twttr_stock(symbol)['score']['ret']
    result[symbol] = corr
result