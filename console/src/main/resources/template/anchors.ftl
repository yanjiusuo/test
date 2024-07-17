<div class="table-of-contents">
    <ul>
        <#list anchors as anchor>
            <li><a href="#${anchor.id}">${anchor.title}</a>
                <ul>
                    <#list anchor.children as child>
                        <li><a href="#${child.id}">${child.title}
                            </a><a id="${child.title}"> </a></li>
                    </#list>
                </ul>
            </li>
        </#list>


    </ul>
</div>