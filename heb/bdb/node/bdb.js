const bdb = require("./BrownDriverBriggs.json");
const lexicon = bdb.lexicon.part;
const { keys } = Object;

// xml:lang => heb,arc
// id => a,b,c -- xa,xb,xc
// section => [Section]
// title => א,ב,ג
//
/* Section
  {
    {
      id: [ 'xn.aa' ], 
      entry: [Entry], 
      page?: [],
    }
  }
  * */
//
/* Entry
   { 
      _: '...',
      id: [ '' ],
      type: [ 'root' ],
      cite: [ '' ],
      mod: [ '' ],
      w: [ '', {}],
      pos: [ 'n.m' ],
      ste: [ '' ],
      def: [ '', '' ],
      ref: [ {} ],
      status: {{ _: 'done', p: ['1'] }},
      sense: [ { _: '', n: [ '1' ], def: [ '', '' ] }, {} ]
   }
*/

const pipe = (...fns) => (initial) =>
  fns.reduce((prev, apply) => apply(prev), initial);

/*
 {
    ר: {
      heb: {
        //
      },
      arc: {
        //
      }
    }
 }
 * */

const fromLex = ({
  id: [id],
  title: [letter],
  "xml:lang": [lang],
  section,
}) => ({ id, letter, section, lang });

const parseLex = (lex) => (d) => ({ raw: lex.map(fromLex), d });

const applyLetters = ({ raw, d }) => ({
  d: {},
  raw: raw.reduce((acc, { id, letter, lang, section }) => {
    return {
      ...acc,
      [letter]: {
        ...(acc[letter] || {}),
        [lang]: { id, section },
      },
    };
  }, d),
});

const flattenSections = ({ raw }) => ({
  raw: keys(raw).reduce(
    (racc, letter) => ({
      ...racc,
      [letter]: {
        ...racc[letter],
        ...keys(racc[letter]).reduce(
          (lacc, lang) => ({
            ...lacc[lang],
            [lang]: {
              entries: lacc[lang],
            },
          }),
          racc[letter]
        ),
      },
    }),
    raw
  ),
});

const final = ({ d }) => d;
const raw = ({ raw }) => raw;

const apply = pipe(parseLex(lexicon), applyLetters, flattenSections, raw);

const applied = apply({});
console.log(applied);
