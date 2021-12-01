const bdb = require("./BrownDriverBriggs.json");
const lexicon = bdb.lexicon.part;

// xml:lang => heb,arc
// id => a,b,c -- xa,xb,xc
// section => [{id: [ 'xn.aa' ], entry: [{ _: '' }], page?: []}]
// title => א,ב,ג
const pipe = (...fns) => {
  return (initial) => fns.reduce((prev, apply) => apply(prev), initial);
};

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
  raw,
  d: raw.reduce((acc, { id, letter, lang, section }) => {
    return {
      ...acc,
      [letter]: {
        ...(acc[letter] || {}),
        [lang]: { id, section },
      },
    };
  }, d),
});

const final = ({ d }) => d;

pipe(
  parseLex(lexicon),
  applyLetters,
  final,
  console.log
)({});
