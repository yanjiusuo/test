<!DOCTYPE html>
<html>
<head>
    <title>cee</title>
    <meta charset="utf-8"/>
    <style>@charset "UTF-8";
        html,
        body,
        h1,
        h2,
        h3,
        h4,
        h5,
        h6,
        p,
        blockquote {
            margin: 0;
            padding: 0;
            font-weight: normal;
        }


        body {
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Helvetica, "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", SimSun, sans-serif;
            font-size: 13px;
            line-height: 25px;
            color: #393838;
            position: relative;
        }

        table {
            margin: 10px 0 15px 0;
            border-collapse: collapse;
        }

        td,
        th {
            border: 1px solid #ddd;
            padding: 3px 10px;
        }

        th {
            padding: 5px 10px;
        }

        a, a:link, a:visited {
            color: #34495e;
            text-decoration: none;
        }

        a:hover, a:focus {
            color: #59d69d;
            text-decoration: none;
        }

        a img {
            border: none;
        }

        p {
            padding-left: 10px;
            margin-bottom: 9px;
        }

        h1,
        h2,
        h3,
        h4,
        h5,
        h6 {
            color: #404040;
            line-height: 36px;
        }

        h1 {
            color: #2c3e50;
            font-weight: 600;
            margin-bottom: 16px;
            font-size: 32px;
            padding-bottom: 16px;
            border-bottom: 1px solid #ddd;
            line-height: 50px;
            max-width: 400px;
            width: 400px;
            word-break: break-word;
        }

        h2 {
            font-size: 28px;
            padding-top: 10px;
            padding-bottom: 10px;
        }

        h3 {
            clear: both;
            font-weight: 400;
            margin-top: 20px;
            margin-bottom: 20px;
            border-left: 3px solid #59d69d;
            padding-left: 8px;
            font-size: 18px;
        }

        h4 {
            font-size: 16px;
        }

        h5 {
            font-size: 14px;
        }

        h6 {
            font-size: 13px;
        }

        hr {
            margin: 0 0 19px;
            border: 0;
            border-bottom: 1px solid #ccc;
        }

        blockquote {
            padding: 13px 13px 21px 15px;
            margin-bottom: 18px;
            font-family: georgia, serif;
            font-style: italic;
        }

        blockquote:before {
            font-size: 40px;
            margin-left: -10px;
            font-family: georgia, serif;
            color: #eee;
        }

        blockquote p {
            font-size: 14px;
            font-weight: 300;
            line-height: 18px;
            margin-bottom: 0;
            font-style: italic;
        }

        code,
        pre {
            font-family: Monaco, Andale Mono, Courier New, monospace;
        }

        code {
            background-color: #fee9cc;
            color: rgba(0, 0, 0, 0.75);
            padding: 1px 3px;
            font-size: 12px;
            -webkit-border-radius: 3px;
            -moz-border-radius: 3px;
            border-radius: 3px;
        }

        pre {
            display: block;
            padding: 14px;
            margin: 0 0 18px;
            line-height: 16px;
            font-size: 11px;
            width: 450px;
            max-width: 450px;
            border: 1px solid #d9d9d9;
            white-space: pre-wrap;
            word-wrap: break-word;
            background: #f6f6f6;
        }

        pre code {
            background-color: #f6f6f6;
            color: #737373;
            font-size: 11px;
            padding: 0;
            width: 440px;
            max-width: 440px;
        }

        sup {
            font-size: 0.83em;
            vertical-align: super;
            line-height: 0;
        }

        * {
            -webkit-print-color-adjust: exact;
        }

        @media print {
            body,
            code,
            pre code,
            h1,
            h2,
            h3,
            h4,
            h5,
            h6 {
                color: black;
            }

            table,
            pre {
                page-break-inside: avoid;
            }
        }

        html,
        body {
            height: 100%;
        }

        .table-of-contents {
            position: fixed;
            top: 61px;
            left: 0;
            bottom: 0;
            overflow-x: hidden;
            overflow-y: auto;
            width: 260px;
        }

        .table-of-contents > ul > li > a {
            font-size: 20px;
            margin-bottom: 16px;
            margin-top: 16px;
        }

        .table-of-contents ul {
            overflow: auto;
            margin: 0px;
            height: 100%;
            padding: 0px 0px;
            box-sizing: border-box;
            list-style-type: none;
        }

        .table-of-contents ul li {
            padding-left: 20px;
        }

        .table-of-contents a {
            padding: 2px 0px;
            display: block;
            text-decoration: none;
        }

        .content-right {
            max-width: 700px;
            flex-grow: 1;
        }

        .content-right h2:target {
            padding-top: 80px;
        }

        body > p {
            margin-left: 30px;
        }

        body > table {
            margin-left: 30px;
        }

        body > pre {
            margin-left: 30px;
        }

        .curProject {
            position: fixed;
            top: 20px;
            font-size: 25px;
            color: black;
            margin-left: -240px;
            width: 240px;
            padding: 5px;
            line-height: 25px;
            box-sizing: border-box;
        }

        .g-doc {
            margin-top: 56px;
            padding-top: 24px;
            display: flex;
        }

        .curproject-name {
            font-size: 42px;
        }

        .m-header {
            background: #32363a;
            height: 56px;
            line-height: 56px;
            padding-left: 60px;
            display: flex;
            align-items: center;
            position: fixed;
            z-index: 9;
            top: 0;
            left: 0;
            right: 0;
        }

        .m-header .title {
            font-size: 22px;
            color: #fff;
            font-weight: normal;
            -webkit-font-smoothing: antialiased;
            margin: 0;
            margin-left: 16px;
            padding: 0;
            line-height: 56px;
            border: none;
        }

        .m-header .nav {
            color: #fff;
            font-size: 16px;
            position: absolute;
            right: 32px;
            top: 0;
        }

        .m-header .nav a {
            color: #fff;
            margin-left: 16px;
            padding: 8px;
            transition: color .2s;
        }

        .m-header .nav a:hover {
            color: #59d69d;
        }

        .m-footer {
            border-top: 1px solid #ddd;
            padding-top: 16px;
            padding-bottom: 16px;
        }

        /*# sourceMappingURL=defaultTheme.css.map */
    </style>
</head>
<body>

<div class="g-doc">
    <div id="right" class="content-right">
       ${right}

        <footer class="m-footer">
            <p>Build by <a href="http://console.paas.jd.com/">藏经阁一体化-在线联调</a>.</p>
        </footer>
    </div>
</div>
</body>
</html>