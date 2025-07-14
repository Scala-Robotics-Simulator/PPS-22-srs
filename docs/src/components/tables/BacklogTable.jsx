export function BacklogTable({ data }) {
  const totals = data.reduce(
    (acc, item) => {
      acc.stima += item.stima;
      acc.effettivo += item.effettivo;
      acc.sprints = acc.sprints.map((s, i) => s + (item.sprints[i] || 0));
      return acc;
    },
    { stima: 0, effettivo: 0, sprints: Array(5).fill(0) }
  );

  return (
    <table>
      <thead>
        <tr>
          <th>Id</th>
          <th>Item</th>
          <th>Stima (h)</th>
          <th>Effettivo (h)</th>
          <th>S1</th>
          <th>S2</th>
          <th>S3</th>
          <th>S4</th>
          <th>S5</th>
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
