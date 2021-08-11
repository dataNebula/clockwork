import defaultSettings from '@/settings'

const title = defaultSettings.title || 'Clockwork';

export default function getPageTitle(pageTitle) {
    if (pageTitle) {
        return `${pageTitle} - ${title}`
    }
    return `${title}`
}
