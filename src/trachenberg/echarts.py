from IPython.core.display import HTML
import uuid
import json


def init_echarts():
    code = """require.config({
                        paths: {
                            echarts: 'https://cdnjs.cloudflare.com/ajax/libs/echarts/4.0.4/echarts-en'
                        }});
            require(['echarts'], function(echarts){
            window.echarts = echarts
        });"""
    return HTML("<script>{}</script>".format(code))


def plot(opts, width=900, height=400):
    id = str(uuid.uuid1())
    code = """console.log("sex")
              var chart = echarts.init(document.getElementById("{id}"));
              chart.setOption({opts});
              """.format(**{'id': id, 'opts': json.dumps(opts)})
    html = """<div>
                <div id={id} style="width:{width}px;
                                    height:{height}px;">
                </div>
                <script>{code}</script>
              </div>""".format(**{'id': id, 'width': width, 'height': height, 'code': code})
    return HTML(html)
