import {type CssNode, generate, type List, type ListItem, parse, walk} from 'css-tree'

const htmlFremTilRedigerbartFelt = (elementer: Element[]) => {
    let funnetRedigerbartInnhold = false
    const prefiks: string[] = []
    elementer.forEach((el) => {
        if (el.querySelector(`.data-editable`)) {
            funnetRedigerbartInnhold = true
        } else if (!funnetRedigerbartInnhold && !el.hasAttribute('data-hidden')) {
            prefiks.push(el.outerHTML)
        }
    })
    return prefiks
}



export function finnRedigerbartInnhold(html: string): string {
    const parser = new DOMParser()
    const doc = parser.parseFromString(html, 'text/html')
    const element = doc.querySelector(`.data-editable`)
    const redigerbartInnholdHtml = element ? element.innerHTML : ''
    return toXhtml(redigerbartInnholdHtml)

}


export function utledPrefiksInnhold(xhtml: string): string {
    const seksjoner = seksjonSomKanRedigeres(xhtml)
    return htmlFremTilRedigerbartFelt(seksjoner).join('')
}

export function utledPostfixInnhold(xhtml: string): string {
    const seksjoner = seksjonSomKanRedigeres(xhtml)
    return htmlFremTilRedigerbartFelt(seksjoner.reverse()).reverse().join('')
}

const seksjonSomKanRedigeres = (html: string) => {
    const heleBrevet = new DOMParser().parseFromString(html, 'application/xhtml+xml')
    return Array.from(heleBrevet.querySelectorAll('section'))
}

export function utledStiler(html: string): string {
    const heleBrevet = new DOMParser().parseFromString(html, 'text/html')
    const stiler = heleBrevet?.querySelector('style')?.innerHTML
    if (stiler) {
        const styleAst = parse(stiler)

        walk(styleAst, (node: CssNode, item: ListItem<CssNode>, list: List<CssNode>) => {
            if (node.type === 'Atrule' && node.name === 'page') list.remove(item)
            if ('ClassSelector' === node.type || 'IdSelector' === node.type || 'TypeSelector' === node.type) {
                if (node.name === 'body') {
                    node.name = 'brev-wrapper'
                } else {
                    switch (node.type) {
                        case 'ClassSelector':
                            node.name = `brev-wrapper .${node.name}`
                            break
                        case 'IdSelector':
                            node.name = `brev-wrapper #${node.name}`
                            break
                        case 'TypeSelector':
                            node.name = `brev-wrapper ${node.name}`
                            break
                        default:
                            break
                    }
                }
                node.type = 'ClassSelector'
            }
        })

        return generate(styleAst)
    }
    return ''
}

export const toXhtml = (html: string) => {
    const doc = new DOMParser().parseFromString(html, 'text/html')
    return new XMLSerializer().serializeToString(doc)
}
