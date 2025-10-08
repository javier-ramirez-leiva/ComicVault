import { ComicsDatabase } from "./comics";

export interface ComicCard {
    setHovered(isHovered: boolean): void,
    getComic(): ComicsDatabase
}
