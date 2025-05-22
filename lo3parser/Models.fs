module Models

type Fandom = { name: string; label: option<string> }

type Work =
    { id: string
      title: string
      authors: list<string> }
