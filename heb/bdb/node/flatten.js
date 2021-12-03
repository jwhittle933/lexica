const fs = require("fs");
const bdb = require("./BrownDriverBriggs.json");
const lexicon = bdb.lexicon.part;

function flatten(obj, keySeparator = ".") {
  const flattenRecursive = (obj, parentProperty, propertyMap = {}) => {
    for (const [key, value] of Object.entries(obj)) {
      const property = parentProperty
        ? `${parentProperty}${keySeparator}${key}`
        : key;
      if (value && typeof value === "object") {
        flattenRecursive(value, property, propertyMap);
      } else {
        propertyMap[property] = value;
      }
    }
    return propertyMap;
  };
  return flattenRecursive(obj);
}

fs.writeFileSync(
  "flag_bdb.json",
  JSON.stringify(
    lexicon.map((l) => {
      const {
        id: [id],
        title: [title],
        "xml:lang": [lang],
        section,
      } = l;
      return flatten({ id, title, lang, section });
    }),
    null,
    2
  )
);
