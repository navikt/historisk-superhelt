
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
export const toXhtml = (html: string) => {
    const doc = new DOMParser().parseFromString(html, 'text/html')
    return new XMLSerializer().serializeToString(doc)
}
