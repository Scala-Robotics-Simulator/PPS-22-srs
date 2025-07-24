export function BacklogTable({ data }) {
  // Determine the maximum number of sprints from the data
  const maxSprints = data.length > 0 ? Math.max(...data.map(item => item.sprints.length)) : 0;
  
  const totals = data.reduce(
    (acc, item) => {
      acc.stima += item.stima;
      acc.effettivo += item.effettivo;
      acc.sprints = acc.sprints.map((s, i) => s + (item.sprints[i] || 0));
      return acc;
    },
    { stima: 0, effettivo: 0, sprints: Array(maxSprints).fill(0) }
  );

  return (
    <table>
      <thead>
        <tr>
          <th>Id</th>
          <th>Item</th>
          <th>Stima (h)</th>
          <th>Effettivo (h)</th>
          {Array.from({ length: maxSprints }, (_, i) => (
            <th key={i}>S{i + 1}</th>
          ))}
        </tr>
      </thead>
      <tbody>
        {data.map(({ id, item, stima, effettivo, sprints }) => (
          <tr key={id}>
            <td>{id}</td>
            <td>{item}</td>
            <td>{stima}</td>
            <td>{effettivo}</td>
            {sprints.map((val, i) => (
              <td key={i}>{val}</td>
            ))}
          </tr>
        ))}
        <tr>
          <td colSpan={2}><strong>TOT</strong></td>
          <td><strong>{totals.stima}</strong></td>
          <td><strong>{totals.effettivo}</strong></td>
          {totals.sprints.map((s, i) => (
            <td key={i}><strong>{s}</strong></td>
          ))}
        </tr>
      </tbody>
    </table>
  );
}
